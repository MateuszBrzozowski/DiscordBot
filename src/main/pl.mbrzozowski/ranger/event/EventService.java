package ranger.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ranger.Repository;
import ranger.embed.EmbedInfo;
import ranger.embed.EmbedSettings;
import ranger.event.reminder.CreateReminder;
import ranger.event.reminder.Timers;
import ranger.helpers.*;
import ranger.model.MemberOfServer;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

@Service
public class EventService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private List<ranger.event.ActiveEvent> activeEvents = new ArrayList<>();
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    private HashMap<String, TextChannel> textChannelsUser = new HashMap<>();


    public void initialize() {
        getAllDatabase();
        checkAllListOfEvents();
        CleanerEventChannel cleanerEventChannel = new CleanerEventChannel();
        cleanerEventChannel.clean();
    }


    private void getAllDatabase() {
        EventDatabase eventDatabase = new EventDatabase();
        downladMatchesDB(eventDatabase);
        downloadPlayersInMatechesDB(eventDatabase);
    }

    private void downladMatchesDB(EventDatabase eventDatabase) {
        ResultSet resultSet = eventDatabase.getAllEvents();
        List<ranger.event.ActiveEvent> matchesToDeleteDB = new ArrayList<>();
        this.activeEvents.clear();
        List<TextChannel> textChannels = Repository.getJda().getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String channelID = resultSet.getString("channelID");
                        String messageID = resultSet.getString("msgID");
                        ranger.event.ActiveEvent match = new ranger.event.ActiveEvent(channelID, messageID);
                        boolean isActive = false;
                        for (TextChannel tc : textChannels) {
                            if (tc.getId().equalsIgnoreCase(channelID)) {
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive) {
                            activeEvents.add(match);
                            CreateReminder reminder = new CreateReminder(messageID);
                            reminder.create();
                        } else {
                            matchesToDeleteDB.add(match);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        for (ranger.event.ActiveEvent a : matchesToDeleteDB) {
            removeEventDB(a.getMessageID());
        }
    }

    private void downloadPlayersInMatechesDB(EventDatabase eventDatabase) {
        ResultSet resultSet = eventDatabase.getAllPlayers();
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        Boolean mainList = resultSet.getBoolean("mainList");
                        String event = resultSet.getString("event");
                        MemberOfServer memberMy = new MemberOfServer(userID, userName);
                        for (ranger.event.ActiveEvent m : activeEvents) {
                            if (m.getMessageID().equalsIgnoreCase(event)) {
                                if (mainList) {
                                    m.addToMainList(memberMy);
                                } else {
                                    m.addToReserveList(memberMy);
                                }
                            }
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    /**
     * Sprawdza każdy event i jeżeli data i czas jest przeszły (event się wydarzył) wyłacza buttony i usuwa z bazy.
     */
    private void checkAllListOfEvents() {
        for (ranger.event.ActiveEvent ae : activeEvents) {
            TextChannel channel = Repository.getJda().getTextChannelById(ae.getChannelID());
            channel.retrieveMessageById(ae.getMessageID()).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                List<MessageEmbed.Field> fields = embeds.get(0).getFields();
                String stringDate = fields.get(0).getValue() + " " + fields.get(2).getValue();
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
                LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
                LocalDateTime date = LocalDateTime.parse(stringDate, dateFormat);
                if (date.isBefore(dateNow)) {
                    cancelEvent(ae.getMessageID());
                }
            });
        }
    }

    /**
     * Sprawdza czy event już się wydarzył.
     *
     * @param indexOfActiveMatch Index eventu na liście
     * @return Zwraca true jeśli event się jeszcze nie wydarzył. W innym przypadku zwraca false.
     */
    private boolean eventIsAfter(int indexOfActiveMatch) {
        TextChannel channel = Repository.getJda().getTextChannelById(activeEvents.get(indexOfActiveMatch).getChannelID());
        List<MessageEmbed> embeds = channel.retrieveMessageById(activeEvents.get(indexOfActiveMatch).getMessageID()).complete().getEmbeds();
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        String dateString = fields.get(0).getValue() + " " + fields.get(2).getValue();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
        LocalDateTime dateEvent = LocalDateTime.parse(dateString, dateFormat);
        LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        if (dateEvent.isAfter(dateNow)) {
            return true;
        }
        return false;
    }

    /**
     * Sprawdza czy pozostały trzy godziny do eventu
     *
     * @param indexOfActiveMatch index eventu
     * @return Zwraca true jeżeli pozostały trzy godziny lub mniej do eventu, w innym przypadku zwraca false
     */
    private boolean threeHoursToEvent(int indexOfActiveMatch) {
        TextChannel channel = Repository.getJda().getTextChannelById(activeEvents.get(indexOfActiveMatch).getChannelID());
        List<MessageEmbed> embeds = channel.retrieveMessageById(activeEvents.get(indexOfActiveMatch).getMessageID()).complete().getEmbeds();
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        String dateString = fields.get(0).getValue() + " " + fields.get(2).getValue();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
        LocalDateTime eventDateTime = LocalDateTime.parse(dateString, dateFormat);
        eventDateTime = eventDateTime.minusHours(3);
        eventDateTime.atZone(ZoneId.of("Europe/Paris"));
        LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        if (dateNow.isAfter(eventDateTime)) {
            return true;
        }
        return false;
    }

    public void createNewEvent(String[] message, String userID) {
        String userName = Users.getUserNicknameFromID(userID);
        RangerLogger.info(userName + " - tworzy nowy event.");
        if (checkMessage(message)) {
            String nameEvent = getEventName(message);
            String date = getDate(message);
            String time = getTime(message);
            String description = getDescription(message);
            boolean ac = searchParametrInMessage(message, "-ac");
            boolean r = searchParametrInMessage(message, "-r");
            boolean c = searchParametrInMessage(message, "-c");
            if (nameEvent != null && date != null && time != null) {
                if (Validation.eventDateTimeAfterNow(date + " " + time)) {
                    if (ac) createEventChannel(userID, nameEvent, date, time, description, 1);
                    else if (r) createEventChannel(userID, nameEvent, date, time, description, 2);
                    else createEventChannel(userID, nameEvent, date, time, description, 3);
                } else {
                    EmbedInfo.dateTimeIsBeforeNow(userID);
                }
            } else {
                RangerLogger.info("Nieprawidłowe lub puste dane w obowiązkowych parametrach -name/-date/-time");
            }
        } else {
            RangerLogger.info("Brak wymaganych parametrów -name <nazwa> -date <data> -time <czas>");
        }
    }

    /**
     * @param userID      ID uzytkownika
     * @param nameEvent   nazwa eventu
     * @param date        kiedy tworzymy event
     * @param time        o której jest event
     * @param description opis eventu
     * @param whoVisable  1 - rekrut + clanMember; 2 - rekrut
     */
    private void createEventChannel(String userID, String nameEvent, String date, String time, String description, int whoVisable) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        String creatorName = Users.getUserNicknameFromID(userID);
        if (guild == null) {
            return;
        }
        List<Category> categories = guild.getCategories();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_EVENT_ID)) {
                if (whoVisable == 1 || whoVisable == 2) {
                    guild.createTextChannel(EmbedSettings.GREEN_CIRCLE + nameEvent + "-" + date + "-" + time, cat)
                            .addPermissionOverride(guild.getPublicRole(), null, permissions)
                            .addRolePermissionOverride(Long.parseLong(RoleID.RECRUT_ID), permissions, null)
                            .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                            .queue(textChannel -> {
                                if (whoVisable == 1) {
                                    createList(creatorName, textChannel, nameEvent, date, time, description, 1);
                                } else {
                                    createList(creatorName, textChannel, nameEvent, date, time, description, 2);
                                }

                            });
                } else {
                    guild.createTextChannel(EmbedSettings.GREEN_CIRCLE + nameEvent + "-" + date + "-" + time, cat).queue(textChannel -> {
                        createList(creatorName, textChannel, nameEvent, date, time, description, 3);
                    });
                }
                break;
            }
        }
    }

    /**
     * @param userName    Nazwa użytkownika, który towrzy listę zapisów
     * @param textChannel ID kanału na którym jest tworzona lista
     * @param nameEvent   Nazwa eventu, który tworzymy
     * @param date        Data kiedy tworzymy event
     * @param time        Czas o której jest event
     * @param description Opis eventu
     * @param whoPing     1 - rekrut + clanMember; 2-rekrut; 3- tylko Clan Member
     */
    private void createList(String userName, TextChannel textChannel, String nameEvent, String date, String time, String description, int whoPing) {
        String msg = "";
        if (whoPing == 1) {
            msg = "<@&" + RoleID.CLAN_MEMBER_ID + "> <@&" + RoleID.RECRUT_ID + "> Zapisy!";
        } else if (whoPing == 2) {
            msg = "<@&" + RoleID.RECRUT_ID + "> Zapisy!";
        } else if (whoPing == 3) {
            msg = "<@&" + RoleID.CLAN_MEMBER_ID + "> Zapisy!";
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(nameEvent);
        if (description != "") {
            builder.setDescription(description);
        }
        builder.addField(EmbedSettings.WHEN_DATE, date, true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.WHEN_TIME, time, true);
        builder.addBlankField(false);
        builder.addField(EmbedSettings.NAME_LIST + "(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.NAME_LIST_RESERVE + "(0)", ">>> -", true);
        builder.setFooter("Utworzony przez " + userName);

        try {
            textChannel.sendMessage(msg).setEmbeds(builder.build()).setActionRow(
                            Button.primary(ComponentId.EVENTS_SIGN_IN, "Zapisz"),
                            Button.secondary(ComponentId.EVENTS_SIGN_IN_RESERVE, "Rezerwa"),
                            Button.danger(ComponentId.EVENTS_SIGN_OUT, "Wypisz"))
                    .queue(message -> {
                        MessageEmbed mOld = message.getEmbeds().get(0);
                        String msgID = message.getId();
                        message.editMessageEmbeds(mOld).setActionRow(Button.primary(ComponentId.EVENTS_SIGN_IN + msgID, "Zapisz"),
                                Button.secondary(ComponentId.EVENTS_SIGN_IN_RESERVE + msgID, "Rezerwa"),
                                Button.danger(ComponentId.EVENTS_SIGN_OUT + msgID, "Wypisz")).queue();
                        message.pin().queue();
                        ranger.event.ActiveEvent event = new ranger.event.ActiveEvent(textChannel.getId(), msgID, nameEvent);
                        activeEvents.add(event);
                        addEventDB(event);
                        CreateReminder reminder = new CreateReminder(date, time, message.getId());
                        reminder.create();
                    });
        } catch (IllegalArgumentException e) {
            RangerLogger.info("Zbudowanie listy niemożliwe. Maksymalna liczba znaków\n" +
                    "Nazwa eventu - 256\n" +
                    "Tekst (opis eventu) - 2048");
        }
    }

    private String getDescription(String[] message) {
        int indexStart = getIndex(message, "-o");
        if (indexStart > 0) {
            int indexEnd = getIndexEnd(message, indexStart);
            if (indexStart >= indexEnd) {
                return "";
            } else {
                String description = "";
                for (int i = indexStart + 1; i <= indexEnd; i++) {
                    description += message[i] + " ";
                }
                return description;
            }
        }
        return "";
    }


    private String getEventName(String[] message) {
        int indexStart = getIndex(message, "-name");
        int indexEnd = getIndexEnd(message, indexStart);
        if (indexStart >= indexEnd) {
            return null;
        } else {
            String name = "";
            for (int i = indexStart + 1; i <= indexEnd; i++) {
                name += message[i] + " ";
            }
            return name;
        }
    }

    private String getDate(String[] message) {
        int indexStart = getIndex(message, "-date");
        if (!isEnd(message[indexStart + 1])) {
            if (Validation.isDateFormat(message[indexStart + 1]))
                return message[indexStart + 1];
        }

        return null;
    }

    private String getTime(String[] message) {
        int indexStart = getIndex(message, "-time");
        String time = message[indexStart + 1];
        if (!isEnd(time)) {
            if (time.length() == 4) {
                time = "0" + time;
            }
            if (Validation.isTimeFormat(time)) {
                return time;
            }
        }
        return null;
    }

    private boolean searchParametrInMessage(String[] message, String s) {
        for (int i = 1; i < message.length; i++) {
            if (message[i].equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private int getIndexEnd(String[] message, int indexStart) {
        for (int i = indexStart + 1; i < message.length; i++) {
            if (isEnd(message[i])) {
                return i - 1;
            }
        }
        return indexStart;
    }

    private boolean isEnd(String s) {
        if (s.equalsIgnoreCase("-name")) return true;
        else if (s.equalsIgnoreCase("-date")) return true;
        else if (s.equalsIgnoreCase("-time")) return true;
        else if (s.equalsIgnoreCase("-o")) return true;
        else if (s.equalsIgnoreCase("-ac")) return true;
        else if (s.equalsIgnoreCase("-r")) return true;
        else if (s.equalsIgnoreCase("-c")) return true;
        else return false;
    }

    private int getIndex(String[] message, String s) {
        for (int i = 0; i < message.length; i++) {
            if (message[i].equalsIgnoreCase(s)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param message wpisana przez użytkownika
     *                Wiadomość musi posiadać 3 obowiazkowe parametry
     *                -name - nazwa eventu
     *                -data - data eventu
     *                -czas - kiedy odbywan sie event
     *                Moze i nie musi zawierac 4 parametru
     *                -opis - opis eventu
     * @return true - jeżeli zostały wpisane wszystkie 3 parametry; false - jeżeli parametry zostały nie zostały
     * wpisane prawidłowo
     */
    public boolean checkMessage(String[] message) {
        boolean name = false;
        boolean date = false;
        boolean time = false;
        for (String s : message) {
            if (s.equalsIgnoreCase("-name")) {
                name = true;
            } else if (s.equalsIgnoreCase("-date")) {
                date = true;
            } else if (s.equalsIgnoreCase("-time")) {
                time = true;
            }
        }
        if (name && date && time) {
            return true;
        } else {
            return false;
        }

    }

    private void addEventDB(ranger.event.ActiveEvent match) {
        EventDatabase edb = new EventDatabase();
        edb.addEvent(match.getChannelID(), match.getMessageID());
    }

    /**
     * @param indexOfMatch index na liscie eventu
     */
    public void updateEmbed(int indexOfMatch) {
        String channelID = activeEvents.get(indexOfMatch).getChannelID();
        String messageID = activeEvents.get(indexOfMatch).getMessageID();
        Repository.getJda().getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed mOld = embeds.get(0);
            List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
            String mainList = activeEvents.get(indexOfMatch).getStringOfMainList();
            String reserveList = activeEvents.get(indexOfMatch).getStringOfReserveList();

            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i == 4) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(EmbedSettings.NAME_LIST + "(" + activeEvents.get(indexOfMatch).getMainList().size() + ")", ">>> " + mainList, true);
                    fieldsNew.add(fieldNew);
                } else if (i == 6) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(EmbedSettings.NAME_LIST_RESERVE + "(" + activeEvents.get(indexOfMatch).getReserveList().size() + ")", ">>> " + reserveList, true);
                    fieldsNew.add(fieldNew);
                } else {
                    fieldsNew.add(fieldsOld.get(i));
                }
            }

            int color;
            if (activeEvents.get(indexOfMatch).getMainList().size() >= 9) {
                color = Color.GREEN.getRGB();
            } else {
                color = Color.YELLOW.getRGB();
            }

            MessageEmbed m = new MessageEmbed(mOld.getUrl()
                    , mOld.getTitle()
                    , mOld.getDescription()
                    , mOld.getType()
                    , mOld.getTimestamp()
                    , color
                    , mOld.getThumbnail()
                    , mOld.getSiteProvider()
                    , mOld.getAuthor()
                    , mOld.getVideoInfo()
                    , mOld.getFooter()
                    , mOld.getImage()
                    , fieldsNew);
            message.editMessageEmbeds(m).queue();

        });
    }

    public String getActiveEventsIndexAndName() {
        String result = "";
        for (int i = 0; i < activeEvents.size(); i++) {
            result += i + 1 + " : " + activeEvents.get(i).getName() + "\n";
        }
        return result;
    }


    /**
     * @param messageID ID wiadomości w której jest lista z zapisami na event
     * @return zwraca index eventu.; Zwraca -1 jeżeli eventu nie ma.
     */
    public int getIndexActiveEvent(String messageID) {
        for (int i = 0; i < activeEvents.size(); i++) {
            if (messageID.equalsIgnoreCase(activeEvents.get(i).getMessageID())) {
                return i;
            }
        }
        return -1;
    }

    public int isActiveMatchChannelID(String channelID) {
        for (int i = 0; i < activeEvents.size(); i++) {
            if (channelID.equalsIgnoreCase(activeEvents.get(i).getChannelID())) {
                return i;
            }
        }
        return -1;
    }

    public void buttonClick(ButtonInteractionEvent event, int indexOfActiveMatch, ButtonClickType buttonClick) {
        String userName = Users.getUserNicknameFromID(event.getUser().getId());
        String userID = event.getUser().getId();
        if (eventIsAfter(indexOfActiveMatch)) {
            switch (buttonClick) {
                case SIGN_IN:
                    activeEvents.get(indexOfActiveMatch).addToMainList(userID, userName, event);
                    break;
                case SIGN_IN_RESERVE:
                    if (!userOnMainList(indexOfActiveMatch, userID)) {
                        activeEvents.get(indexOfActiveMatch).addToReserveList(userID, userName, event);
                    } else if (!threeHoursToEvent(indexOfActiveMatch)) {
                        activeEvents.get(indexOfActiveMatch).addToReserveList(userID, userName, event);
                    } else {
                        EmbedInfo.youCantSignReserve(userID, activeEvents.get(indexOfActiveMatch).getMessageID());
                        RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z głownej listy na rezerwową ["
                                + activeEvents.get(indexOfActiveMatch).getName() + "] - Czas do eventu 3h lub mniej.");
                    }
                    break;
                case SIGN_OUT:
                    if (userOnMainList(indexOfActiveMatch, userID) || userOnReserveList(indexOfActiveMatch, userID)) {
                        if (!threeHoursToEvent(indexOfActiveMatch)) {
                            activeEvents.get(indexOfActiveMatch).removeFromEvent(userID);
                        } else {
                            EmbedInfo.youCantSingOut(userID, activeEvents.get(indexOfActiveMatch).getMessageID());
                            RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z eventu ["
                                    + activeEvents.get(indexOfActiveMatch).getName() + "] - Czas do eventu 3h lub mniej.");
                        }
                    }
                    break;
            }
        } else {
            RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] Kliknął w przycisk ["
                    + activeEvents.get(indexOfActiveMatch).getName() + "] - Event się już rozpoczął.");
            EmbedInfo.eventIsBefore(userID);
            disableButtons(event.getMessageId());
        }
    }

    private boolean userOnMainList(int index, String userID) {
        return activeEvents.get(index).checkMemberOnMainList(userID);
    }

    private boolean userOnReserveList(int index, String userID) {
        return activeEvents.get(index).checkMemberOnReserveList(userID);
    }

    public void deleteChannelByID(String channelID) {
        while (true) {
            int inexOfMatch = isActiveMatchChannelID(channelID);
            if (inexOfMatch == -1) {
                break;
            }
            removeEventDB(activeEvents.get(inexOfMatch).getMessageID());
            activeEvents.remove(inexOfMatch);
        }
    }

    public void cancelEvnetWithInfoForPlayers(String messageID) {
        logger.info("Odwołujemy event");
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            Repository.getJda().getTextChannelById(activeEvents.get(index).getChannelID()).retrieveMessageById(messageID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                List<MessageEmbed.Field> fields = embeds.get(0).getFields();
                String dateTime = getDateAndTimeFromEmbed(fields);
                activeEvents.get(index).sendInfoChanges(EventChanges.REMOVE, dateTime);
                cancelEvent(messageID);
            });
        }
    }

    public void cancelEvent(String messageID) {
        int index = getIndexActiveEvent(messageID);
        RangerLogger.info("Event [" + messageID + "] usunięty z bazy danych.");
        if (index >= 0) {
            disableButtons(messageID);
            removeEventDB(messageID);
            changeTitleRedCircle(activeEvents.get(index).getChannelID());
            activeEvents.remove(index);
            Timers timers = Repository.getTimers();
            timers.cancel(messageID);
        }
    }

    /**
     * Zmienia nazwę kanału. Kołko zielone zamienia się w czerwone.
     *
     * @param channelID ID kanału którego jest zmieniana nazwa
     */
    public void changeTitleRedCircle(String channelID) {
        String buffor = Repository.getJda().getTextChannelById(channelID).getName();
        buffor = buffor.replace(EmbedSettings.GREEN_CIRCLE, EmbedSettings.RED_CIRCLE);
        Repository.getJda().getTextChannelById(channelID).getManager()
                .setName(buffor)
                .queue();
    }

    public void changeTime(String messageID, String time, String userID, boolean notifi) {
        if (!Validation.isTimeFormat(time)) return;
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            TextChannel textChannel = Repository.getJda().getTextChannelById(activeEvents.get(index).getChannelID());
            textChannel.retrieveMessageById(messageID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                MessageEmbed mOld = embeds.get(0);
                List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
                List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
                String date = fieldsOld.get(0).getValue();
                String dateTime = date + " " + time;
                if (!Validation.eventDateTimeAfterNow(dateTime)) {
                    EmbedInfo.dateTimeIsBeforeNow(userID);
                    return;
                }
                textChannel.getManager().setName(EmbedSettings.GREEN_CIRCLE + activeEvents.get(index).getName() + "-" + dateTime).queue();
                for (int i = 0; i < fieldsOld.size(); i++) {
                    if (i == 2) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(":clock930: Godzina", time, true);
                        fieldsNew.add(fieldNew);
                    } else {
                        fieldsNew.add(fieldsOld.get(i));
                    }
                }
                String newDateTime = getDateAndTimeFromEmbed(fieldsNew);
                MessageEmbed mNew = new MessageEmbed(mOld.getUrl()
                        , mOld.getTitle()
                        , mOld.getDescription()
                        , mOld.getType()
                        , mOld.getTimestamp()
                        , mOld.getColorRaw()
                        , mOld.getThumbnail()
                        , mOld.getSiteProvider()
                        , mOld.getAuthor()
                        , mOld.getVideoInfo()
                        , mOld.getFooter()
                        , mOld.getImage()
                        , fieldsNew);
                message.editMessageEmbeds(mNew).queue(message1 -> {
                    updateTimer(messageID);
                    if (notifi) {
                        activeEvents.get(index).sendInfoChanges(EventChanges.CHANGES, newDateTime);
                    }
                });
            });
        }
    }


    public void changeDate(String messageID, String date, String userID, boolean notifi) {
        if (!Validation.isDateFormat(date)) return;
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            TextChannel textChannel = Repository.getJda().getTextChannelById(activeEvents.get(index).getChannelID());
            textChannel.retrieveMessageById(messageID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                MessageEmbed mOld = embeds.get(0);
                List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
                List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
                String time = fieldsOld.get(2).getValue();
                String dateTime = date + " " + time;
                if (!Validation.eventDateTimeAfterNow(dateTime)) {
                    EmbedInfo.dateTimeIsBeforeNow(userID);
                    return;
                }
                textChannel.getManager().setName(EmbedSettings.GREEN_CIRCLE + activeEvents.get(index).getName() + "-" + dateTime).queue();
                for (int i = 0; i < fieldsOld.size(); i++) {
                    if (i == 0) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(":date: Kiedy", date, true);
                        fieldsNew.add(fieldNew);
                    } else {
                        fieldsNew.add(fieldsOld.get(i));
                    }
                }
                String newDateTime = getDateAndTimeFromEmbed(fieldsNew);
                MessageEmbed mNew = new MessageEmbed(mOld.getUrl()
                        , mOld.getTitle()
                        , mOld.getDescription()
                        , mOld.getType()
                        , mOld.getTimestamp()
                        , mOld.getColorRaw()
                        , mOld.getThumbnail()
                        , mOld.getSiteProvider()
                        , mOld.getAuthor()
                        , mOld.getVideoInfo()
                        , mOld.getFooter()
                        , mOld.getImage()
                        , fieldsNew);
                message.editMessageEmbeds(mNew).queue(message1 -> {
                    updateTimer(messageID);
                    if (notifi) {
                        activeEvents.get(index).sendInfoChanges(EventChanges.CHANGES, newDateTime);
                    }
                });
            });
        }
    }


    public void changeDateAndTime(String eventID, String newDate, String newTime, String userID, boolean notifi) {
        if (!Validation.isDateFormat(newDate)) return;
        if (!Validation.isTimeFormat(newTime)) return;
        int index = getIndexActiveEvent(eventID);
        if (index >= 0) {
            TextChannel textChannel = Repository.getJda().getTextChannelById(activeEvents.get(index).getChannelID());
            textChannel.retrieveMessageById(eventID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                MessageEmbed mOld = embeds.get(0);
                List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
                List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
                String dataTime = newDate + " " + newTime;
                if (!Validation.eventDateTimeAfterNow(dataTime)) {
                    EmbedInfo.dateTimeIsBeforeNow(userID);
                    return;
                }
                textChannel.getManager().setName(EmbedSettings.GREEN_CIRCLE + activeEvents.get(index).getName() + dataTime).queue();
                for (int i = 0; i < fieldsOld.size(); i++) {
                    if (i == 0) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(":date: Kiedy", newDate, true);
                        fieldsNew.add(fieldNew);
                    } else if (i == 2) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(":clock930: Godzina", newTime, true);
                        fieldsNew.add(fieldNew);
                    } else {
                        fieldsNew.add(fieldsOld.get(i));
                    }
                }
                String newDateTime = getDateAndTimeFromEmbed(fieldsNew);
                MessageEmbed mNew = new MessageEmbed(mOld.getUrl()
                        , mOld.getTitle()
                        , mOld.getDescription()
                        , mOld.getType()
                        , mOld.getTimestamp()
                        , mOld.getColorRaw()
                        , mOld.getThumbnail()
                        , mOld.getSiteProvider()
                        , mOld.getAuthor()
                        , mOld.getVideoInfo()
                        , mOld.getFooter()
                        , mOld.getImage()
                        , fieldsNew);
                message.editMessageEmbeds(mNew).queue(message1 -> {
                    updateTimer(eventID);
                    if (notifi) {
                        activeEvents.get(index).sendInfoChanges(EventChanges.CHANGES, newDateTime);
                    }
                });
            });
        }
    }

    private void updateTimer(String messageID) {
        Timers timers = Repository.getTimers();
        timers.cancel(messageID);
        CreateReminder reminder = new CreateReminder(messageID);
        reminder.create();
    }

    /**
     * Wyłącza przyciski w zapisach
     *
     * @param messageID ID wiadomości eventu
     */
    public void disableButtons(String messageID) {
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            disableButtons(messageID, activeEvents.get(index).getChannelID());
        }
    }

    /**
     * @param messageID ID wiadomości eventu
     * @param channelID ID kanału na którym znajduję sie event
     */
    public void disableButtons(String messageID, String channelID) {
        TextChannel textChannel = Repository.getJda().getTextChannelById(channelID);
        textChannel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            List<Button> buttons = message.getButtons();
            List<Button> buttonsNew = new ArrayList<>();
            for (Button b : buttons) {
                b = b.asDisabled();
                buttonsNew.add(b);
            }
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessageEmbeds(messageEmbed).setActionRow(buttonsNew).queue();
        });
    }

    public void enableButtons(String messageID) {
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            enableButtons(messageID, activeEvents.get(index).getChannelID());
        }
    }

    public void enableButtons(String messageID, String channelID) {
        TextChannel textChannel = Repository.getJda().getTextChannelById(channelID);
        textChannel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            List<Button> buttons = message.getButtons();
            List<Button> buttonsNew = new ArrayList<>();
            for (Button b : buttons) {
                b = b.asEnabled();
                buttonsNew.add(b);
            }
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessageEmbeds(messageEmbed).setActionRow(buttonsNew).queue();
        });
    }

    private void removeEventDB(String messageID) {
        EventDatabase edb = new EventDatabase();
        edb.removeEvent(messageID);
    }

    public List<MemberOfServer> getMainList(int indexOfEvent) {
        return activeEvents.get(indexOfEvent).getMainList();
    }

    public List<MemberOfServer> getReserveList(int indexOfEvent) {
        return activeEvents.get(indexOfEvent).getReserveList();
    }

    /**
     * @param eventID ID wiadomości w której znajdują się zapisy
     * @return Zwraca ID kanalu na którym znajduje sie wiadomosc, w innym przypadku zwraca pustego Stringa
     */
    public String getChannelID(String eventID) {
        for (ranger.event.ActiveEvent ae : activeEvents) {
            if (ae.getMessageID().equalsIgnoreCase(eventID)) {
                return ae.getChannelID();
            }
        }
        return "";
    }

    /**
     * Wyświetla ile jest aktywnych evnetów
     * oraz wypisuje ID eventu czyli wiadomości w której przechowywana jest lista, ID kanału oraz
     * dane ile jest zapisanych użytkowników. Również wyświetla wszystkich użytkowników.
     *
     * @param privateChannel Kanał użytkownika który chce wyświetlić status aplikacji.
     */
    public void sendInfo(PrivateChannel privateChannel) {
        EmbedBuilder activeEventsBuilder = new EmbedBuilder();
        activeEventsBuilder.setColor(Color.RED);
        activeEventsBuilder.setTitle("Eventy");
        activeEventsBuilder.addField("Aktywnych eventów", String.valueOf(activeEvents.size()), false);
        privateChannel.sendMessageEmbeds(activeEventsBuilder.build()).queue();
        for (ranger.event.ActiveEvent ae : activeEvents) {
            List<MemberOfServer> mainList = ae.getMainList();
            List<MemberOfServer> reserveList = ae.getReserveList();
            String channelName = Repository.getJda().getTextChannelById(ae.getChannelID()).getName();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.WHITE);
            builder.addField("ID eventu", ae.getMessageID(), false);
            builder.addField("ID kanału", ae.getChannelID(), false);
            builder.addField("Nazwa kanału", channelName, false);
            builder.addField("Ilość zapisanych", String.valueOf(ae.getNumberOfSignIn()), true);
            builder.addField("Główna lista", String.valueOf(mainList.size()), true);
            builder.addField("Rezerwowa lista", String.valueOf(reserveList.size()), true);
            String stringMainList = "";
            for (MemberOfServer m : mainList) {
                stringMainList += m.getUserName() + ", ";
            }
            builder.addField("Główna lista", stringMainList, false);
            String stringReserveList = "";
            for (MemberOfServer m : reserveList) {
                stringReserveList += m.getUserName() + ", ";
            }
            builder.addField("Rezerwowa lista", stringReserveList, false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        }
    }

    public String getEventNameFromEmbed(String eventID) {
        int indexActiveEvent = getIndexActiveEvent(eventID);
        return activeEvents.get(indexActiveEvent).getName();
    }

    public String getDateAndTimeFromEmbed(String eventID) {
        int indexActiveEvent = getIndexActiveEvent(eventID);
        String channelID = activeEvents.get(indexActiveEvent).getChannelID();
        Message message = Repository.getJda().getTextChannelById(channelID).retrieveMessageById(eventID).complete();
        List<MessageEmbed> embeds = message.getEmbeds();
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        String date = fields.get(0).getValue();
        String time = fields.get(2).getValue();
        return date + "r., " + time;

    }

    private String getDateAndTimeFromEmbed(List<MessageEmbed.Field> fields) {
        String date = fields.get(0).getValue();
        String time = fields.get(2).getValue();
        return date + "r., " + time;
    }

    public List getAllEventID() {
        List<String> listOfEventsID = new ArrayList<>();
        for (ActiveEvent activeEvent : activeEvents) {
            listOfEventsID.add(activeEvent.getMessageID());
        }
        return listOfEventsID;
    }

    public boolean checkEventIDOnIndex(int chossedIndexOFEvent, String eventID) {
        if (activeEvents.get(chossedIndexOFEvent).getMessageID().equalsIgnoreCase(eventID)) {
            return true;
        } else {
            return false;
        }
    }

    public String getDateFromEmbed(String eventID) {
        int indexActiveEvent = getIndexActiveEvent(eventID);
        String channelID = activeEvents.get(indexActiveEvent).getChannelID();
        Message message = Repository.getJda().getTextChannelById(channelID).retrieveMessageById(eventID).complete();
        List<MessageEmbed> embeds = message.getEmbeds();
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        String value = fields.get(0).getValue();
        return value;
    }

    public String getTimeFromEmbed(String eventID) {
        int indexActiveEvent = getIndexActiveEvent(eventID);
        String channelID = activeEvents.get(indexActiveEvent).getChannelID();
        Message message = Repository.getJda().getTextChannelById(channelID).retrieveMessageById(eventID).complete();
        List<MessageEmbed> embeds = message.getEmbeds();
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        String value = fields.get(2).getValue();
        return value;
    }

    public void removeUserFromEvent(String userID, String eventID) {
        int index = getIndexActiveEvent(eventID);
        if (index >= 0) {
            activeEvents.get(index).removeFromEventManually(userID);
            updateEmbed(index);
        } else {
            RangerLogger.info("Nie ma takiego eventu [" + eventID + "]");
        }
    }

    public void removeUserFromAllEvents(String userID) {
        for (int i = 0; i < activeEvents.size(); i++) {
            boolean remove = activeEvents.get(i).removeFromEventManually(userID);
            if (remove) {
                updateEmbed(i);
            }
        }
    }

    public boolean isActiveEvents() {
        return activeEvents.size() > 0;
    }
}

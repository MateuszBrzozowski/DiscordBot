package event;

import database.DBConnector;
import embed.EmbedHelp;
import embed.EmbedInfo;
import embed.EmbedSettings;
import event.reminder.CreateReminder;
import helpers.*;
import model.MemberMy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class Event {

    private List<ActiveEvent> activeEvents = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String NAME_LIST = ":white_check_mark: Lista ";
    public static final String NAME_LIST_RESERVE = ":regional_indicator_r: Niepewny/Rezerwa ";
    private Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
    private HashMap<String, TextChannel> textChannelsUser = new HashMap<>();

    public void initialize(JDA jda) {
        getAllDatabase(jda);
        checkAllListOfEvents();
    }


    private void getAllDatabase(JDA jda) {
        downladMatchesDB(jda);
        downloadPlayersInMatechesDB();
//        loggingInput();
    }

    private void loggingInput() {
        RangerLogger.info(String.format("Ilość aktywnych eventów: [%d]", activeEvents.size()));
        for (ActiveEvent ae : activeEvents) {
            RangerLogger.info(String.format("Event [%s] - Ilość zapisanych: [%d]", ae.getMessageID(), ae.getNumberOfSignIn()));
        }
    }

    private void downladMatchesDB(JDA jda) {
        ResultSet resultSet = getAllMatches();
        List<ActiveEvent> matchesToDeleteDB = new ArrayList<>();
        this.activeEvents.clear();
        List<TextChannel> textChannels = jda.getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String channelID = resultSet.getString("channelID");
                        String messageID = resultSet.getString("msgID");
                        ActiveEvent match = new ActiveEvent(channelID, messageID);
                        boolean isActive = false;
                        for (TextChannel tc : textChannels) {
                            if (tc.getId().equalsIgnoreCase(channelID)) {
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive) {
                            activeEvents.add(match);
//                            CreateReminder reminder = new CreateReminder(messageID);
//                            reminder.create();
                        } else {
                            matchesToDeleteDB.add(match);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        for (ActiveEvent a : matchesToDeleteDB) {
            removeMemberFromEventDB(a.getMessageID());
            removeMatchDB(a.getMessageID());
        }
    }

    private void downloadPlayersInMatechesDB() {
        ResultSet resultSet = getAllPlayers();
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        Boolean mainList = resultSet.getBoolean("mainList");
                        String event = resultSet.getString("event");
                        MemberMy memberMy = new MemberMy(userID, userName);
                        for (ActiveEvent m : activeEvents) {
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
     * Sprawdza każdy event i jeżeli data i czas jest przeszły (event się wydarzył) wyłacza buttony.
     * Jeżeli event wydarzył się więcej niż 2 dni temu to usuwa z bazy danych.
     */
    private void checkAllListOfEvents() {
        JDA jda = Repository.getJda();
        for (ActiveEvent ae : activeEvents) {
            TextChannel channel = jda.getTextChannelById(ae.getChannelID());
            channel.retrieveMessageById(ae.getMessageID()).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                List<MessageEmbed.Field> fields = embeds.get(0).getFields();
                String stringDate = fields.get(0).getValue() + " " + fields.get(2).getValue();
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
                LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
                LocalDateTime date = LocalDateTime.parse(stringDate, dateFormat);
                if (date.isBefore(dateNow)) {
                    LocalDateTime dateOneDaysBefore = LocalDateTime.now(ZoneId.of("Europe/Paris")).minusDays(1);
                    if (dateOneDaysBefore.isAfter(date)) {
                        removeEvent(ae.getMessageID());
                    }
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
        JDA jda = Repository.getJda();
        TextChannel channel = jda.getTextChannelById(activeEvents.get(indexOfActiveMatch).getChannelID());
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

    private void removeMatchDB(String messageID) {
        String query = "DELETE FROM `event` WHERE msgID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, messageID));
    }

    private void removeMemberFromEventDB(String messageID) {
        String query = "DELETE FROM `players` WHERE event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, messageID));
    }

    private ResultSet getAllMatches() {
        String query = "SELECT * FROM `event`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli event w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE event(" +
                    "msgID VARCHAR(30) PRIMARY KEY," +
                    "channelID VARCHAR(30) NOT NULL)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    private ResultSet getAllPlayers() {
        String query = "SELECT * FROM `players`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli players w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE players(" +
                    "id INT(9) UNSIGNED AUTO_INCREMENT PRIMARY KEY, " +
                    "userID VARCHAR(30)," +
                    "userName VARCHAR(30) NOT NULL," +
                    "mainList BOOLEAN," +
                    "event VARCHAR(30) NOT NULL," +
                    "FOREIGN KEY (event) REFERENCES event(msgID))";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    public void createNewEventFrom3Data(String[] message, String userID) {
        if (Validation.isDateFormat(message[2]) && Validation.isTimeFormat(message[3])) {
            if (Validation.eventDateTimeAfterNow(message[2] + " " + message[3])) {
                createEventChannel(userID, message[1], message[2], message[3], null, 3);
            } else {
                EmbedInfo.dateTimeIsBeforeNow(userID);
            }
        } else {
            EmbedInfo.wrongDateOrTime(userID);
        }
    }

    public void createNewEventFrom4Data(String[] message, String userID) {
        if (Validation.isDateFormat(message[2]) && Validation.isTimeFormat(message[3])) {
            if (Validation.eventDateTimeAfterNow(message[2] + " " + message[3])) {
                if (message[4].equalsIgnoreCase("-ac")) {
                    createEventChannel(userID, message[1], message[2], message[3], null, 1);
                } else if (message[4].equalsIgnoreCase("-r")) {
                    createEventChannel(userID, message[1], message[2], message[3], null, 2);
                }
            } else {
                EmbedInfo.dateTimeIsBeforeNow(userID);
            }
        } else {
            EmbedInfo.wrongDateOrTime(userID);
        }
    }

    public void createNewEventFrom3DataHere(String[] message, String userID, TextChannel channel) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (Validation.isDateFormat(message[2]) && Validation.isTimeFormat(message[3])) {
            if (Validation.eventDateTimeAfterNow(message[2] + " " + message[3])) {
                channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.CLAN_MEMBER_ID), permissions, null).queue();
                createList(Users.getUserNicknameFromID(userID), channel, message[1], message[2], message[3], null, 3);
            } else {
                EmbedInfo.dateTimeIsBeforeNow(userID);
            }
        } else {
            EmbedInfo.wrongDateOrTime(userID);
        }
    }

    public void createNewEventFrom4DataHere(String[] message, String userID, TextChannel channel) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (Validation.isDateFormat(message[2]) && Validation.isTimeFormat(message[3])) {
            if (Validation.eventDateTimeAfterNow(message[2] + " " + message[3])) {
                if (message[4].equalsIgnoreCase("-ac")) {
                    channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.RECRUT_ID), permissions, null).queue();
                    channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.CLAN_MEMBER_ID), permissions, null).queue();
                    createList(Users.getUserNicknameFromID(userID), channel, message[1], message[2], message[3], null, 1);
                } else if (message[4].equalsIgnoreCase("-r")) {
                    channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.RECRUT_ID), permissions, null).queue();
                    createList(Users.getUserNicknameFromID(userID), channel, message[1], message[2], message[3], null, 2);
                }
            } else {
                EmbedInfo.dateTimeIsBeforeNow(userID);
            }
        } else {
            EmbedInfo.wrongDateOrTime(userID);
        }
    }

    public void createNewEventFromSpecificData(String[] message, String userID, TextChannel channel) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
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
                    if (message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
                        if (ac || r) {
                            channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.CLAN_MEMBER_ID), permissions, null).queue();
                            channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.RECRUT_ID), permissions, null).queue();
                        }
                        if (ac) {
                            createList(Users.getUserNicknameFromID(userID), channel, nameEvent, date, time, description, 1);
                        } else if (r) {
                            createList(Users.getUserNicknameFromID(userID), channel, nameEvent, date, time, description, 2);
                        } else if (c) {
                            channel.getManager().putPermissionOverride(guild.getRoleById(RoleID.CLAN_MEMBER_ID), permissions, null).queue();
                            createList(Users.getUserNicknameFromID(userID), channel, nameEvent, date, time, description, 3);
                        } else
                            createList(Users.getUserNicknameFromID(userID), channel, nameEvent, date, time, description, -1);
                    } else {
                        if (ac) createEventChannel(userID, nameEvent, date, time, description, 1);
                        else if (r) createEventChannel(userID, nameEvent, date, time, description, 2);
                        else createEventChannel(userID, nameEvent, date, time, description, 3);
                    }
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

        List<Category> categories = guild.getCategories();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_EVENT_ID)) {
                if (whoVisable == 1 || whoVisable == 2) {
                    guild.createTextChannel(nameEvent + "-" + date + "-" + time, cat)
                            .addPermissionOverride(guild.getPublicRole(), null, permissions)
                            .addRolePermissionOverride(Long.parseLong(RoleID.RECRUT_ID), permissions, null)
                            .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                            .queue(textChannel -> {
                                if (whoVisable == 1)
                                    createList(creatorName, textChannel, nameEvent, date, time, description, 1);
                                else if (whoVisable == 2)
                                    createList(creatorName, textChannel, nameEvent, date, time, description, 2);

                            });
                } else {
                    guild.createTextChannel(nameEvent + "-" + date + "-" + time, cat).queue(textChannel -> {
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
            msg = "<@&" + "RoleID.CLAN_MEMBER_ID" + "> <@&" + " RoleID.RECRUT_ID" + "> Zapisy!";
        } else if (whoPing == 2) {
            msg = "<@&" + "RoleID.RECRUT_ID" + "> Zapisy!";
        } else if (whoPing == 3) {
            msg = "<@&" + "RoleID.CLAN_MEMBER_ID" + "> Zapisy!";
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(nameEvent);
        if (description != "") {
            builder.setDescription(description);
        }
        builder.addField(":date: Kiedy", date, true);
        builder.addBlankField(true);
        builder.addField(":clock930: Godzina", time, true);
        builder.addBlankField(false);
        builder.addField(NAME_LIST + "(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(NAME_LIST_RESERVE + "(0)", ">>> -", true);
        builder.setFooter("Utworzony przez " + userName);
        try {
            textChannel.sendMessage(msg).embed(builder.build()).setActionRow(
                    Button.primary("in_", "Zapisz"),
                    Button.secondary("reserve_", "Niepewny"),
                    Button.danger("out_", "Wypisz"))
                    .queue(message -> {
                        MessageEmbed mOld = message.getEmbeds().get(0);
                        String msgID = message.getId();
                        message.editMessage(mOld).setActionRow(Button.primary("in_" + msgID, "Zapisz"),
                                Button.secondary("reserve_" + msgID, "Niepewny"),
                                Button.danger("out_" + msgID, "Wypisz")).queue();
                        ActiveEvent event = new ActiveEvent(textChannel.getId(), msgID);
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

    private void addEventDB(ActiveEvent match) {
        String query = "INSERT INTO `event` (`channelID`,`msgID`) VALUES (\"%s\",\"%s\")";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, match.getChannelID(), match.getMessageID()));
    }

    public void updateEmbed(@NotNull ButtonClickEvent event, int indexOfMatch) {
        String messageID = event.getMessage().getId();
        event.getChannel().retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed mOld = embeds.get(0);
            List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
            String mainList = activeEvents.get(indexOfMatch).getStringOfMainList();
            String reserveList = activeEvents.get(indexOfMatch).getStringOfReserveList();

            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i == 4) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(NAME_LIST + "(" + activeEvents.get(indexOfMatch).getMainList().size() + ")", ">>> " + mainList, true);
                    fieldsNew.add(fieldNew);
                } else if (i == 6) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(NAME_LIST_RESERVE + "(" + activeEvents.get(indexOfMatch).getReserveList().size() + ")", ">>> " + reserveList, true);
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
            message.editMessage(m).queue();

        });
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

    public void buttonClick(ButtonClickEvent event, int indexOfActiveMatch, ButtonClick buttonClick) {
        String userName = Users.getUserNicknameFromID(event.getUser().getId());
        String userID = event.getUser().getId();
        if (eventIsAfter(indexOfActiveMatch)) {
            switch (buttonClick) {
                case SIGN_IN:
                    activeEvents.get(indexOfActiveMatch).addToMainList(userID, userName, event);
                    break;
                case SIGN_IN_RESERVE:
                    activeEvents.get(indexOfActiveMatch).addToReserveList(userID, userName, event);
                    break;
                case SIGN_OUT:
                    activeEvents.get(indexOfActiveMatch).removeFromMatch(userID);
                    break;
            }
        } else {
            EmbedInfo.eventIsBefore(userID);
            disableButtons(event.getMessageId());
        }
    }

    public void deleteChannelByID(String channelID) {
        while (true) {
            int inexOfMatch = isActiveMatchChannelID(channelID);
            if (inexOfMatch == -1) {
                break;
            }
            RemoveEventDB(activeEvents.get(inexOfMatch).getMessageID());
            activeEvents.remove(inexOfMatch);
        }
    }

    public void removeEvent(String messageID) {
        int index = getIndexActiveEvent(messageID);
        RangerLogger.info("Event [" + messageID + "] usunięty z bazy danych.");
        if (index >= 0) {
            disableButtons(messageID);
            RemoveEventDB(messageID);
            activeEvents.remove(index);
        }
    }

    public void changeTime(String messageID, String time, String userID) {
        if (!Validation.isTimeFormat(time)) return;
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            JDA jda = Repository.getJda();
            TextChannel textChannel = jda.getTextChannelById(activeEvents.get(index).getChannelID());
            textChannel.retrieveMessageById(messageID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                MessageEmbed mOld = embeds.get(0);
                List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
                List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
                String dateTime = fieldsOld.get(0).getValue() + " " + time;
                if (!Validation.eventDateTimeAfterNow(dateTime)) {
                    EmbedInfo.dateTimeIsBeforeNow(userID);
                    return;
                }
                for (int i = 0; i < fieldsOld.size(); i++) {
                    if (i == 2) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(":clock930: Godzina", time, true);
                        fieldsNew.add(fieldNew);
                    } else {
                        fieldsNew.add(fieldsOld.get(i));
                    }
                }
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
                message.editMessage(mNew).queue();
            });
        }
    }


    public void changeDate(String messageID, String date, String userID) {
        if (!Validation.isDateFormat(date)) return;
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            JDA jda = Repository.getJda();
            TextChannel textChannel = jda.getTextChannelById(activeEvents.get(index).getChannelID());
            textChannel.retrieveMessageById(messageID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                MessageEmbed mOld = embeds.get(0);
                List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
                List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
                String dateTime = date + " " + fieldsOld.get(2).getValue();
                if (!Validation.eventDateTimeAfterNow(dateTime)) {
                    EmbedInfo.dateTimeIsBeforeNow(userID);
                    return;
                }
                for (int i = 0; i < fieldsOld.size(); i++) {
                    if (i == 0) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(":date: Kiedy", date, true);
                        fieldsNew.add(fieldNew);
                    } else {
                        fieldsNew.add(fieldsOld.get(i));
                    }
                }
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
                message.editMessage(mNew).queue();
            });
        }
    }

    public void disableButtons(String messageID) {
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            disableButtons(messageID, activeEvents.get(index).getChannelID());
        }
    }

    public void disableButtons(String messageID, String channelID) {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelID);
        textChannel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            List<Button> buttons = message.getButtons();
            List<Button> buttonsNew = new ArrayList<>();
            for (Button b : buttons) {
                b = b.asDisabled();
                buttonsNew.add(b);
            }
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessage(messageEmbed).setActionRow(buttonsNew).queue();
        });
    }

    public void enableButtons(String messageID) {
        int index = getIndexActiveEvent(messageID);
        if (index >= 0) {
            enableButtons(messageID, activeEvents.get(index).getChannelID());
        }
    }

    public void enableButtons(String messageID, String channelID) {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelID);
        textChannel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            List<Button> buttons = message.getButtons();
            List<Button> buttonsNew = new ArrayList<>();
            for (Button b : buttons) {
                b = b.asEnabled();
                buttonsNew.add(b);
            }
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessage(messageEmbed).setActionRow(buttonsNew).queue();
        });
    }

    private void RemoveEventDB(String messageID) {
        String queryPlayers = "DELETE FROM players WHERE event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(queryPlayers, messageID));

        String queryEvent = "DELETE FROM event WHERE msgID=\"%s\"";
        connector.executeQuery(String.format(queryEvent, messageID));
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        logger.info("Na kanale znajdują się listy/zapisy na eventy");
        EmbedInfo.removedChannel(event.getChannel());
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deleteChannelByID(event.getChannel().getId());
            event.getGuild().getTextChannelById(event.getChannel().getId()).delete().reason("Event zakończony").queue();
            logger.info("Kanał {} usunięty przez {}", event.getChannel().getName(), event.getAuthor().getName());
        });
        thread.start();
    }

    private String getUserNameFromEvent(GuildMessageReceivedEvent event) {
        String userName = event.getMessage().getMember().getNickname();
        if (userName == null) {
            userName = event.getMessage().getAuthor().getName();
        }
        return userName;
    }

    public void createNewChannel(Guild guild, String userID) {
        String username = Users.getUserNicknameFromID(userID);
        RangerLogger.info("Użytkownik [" + username + "] stworzył nowy kanał.");
        List<Category> categories = guild.getCategories();
        for (Category c : categories) {
            if (c.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_EVENT_ID)) {
                guild.createTextChannel("nowy-event", c)
                        .addPermissionOverride(guild.getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .queue(textChannel -> {
                            textChannelsUser.put(userID, textChannel);
                            EmbedHelp.infoEditEventChannel(userID);
                        });
                break;
            }
        }
    }

    /**
     * @param event Wydarzenie wpisania wiadomości na kanale.
     * @return Zwraca true jeżeli użytkownik stworzył wcześniej kanał na którym pisze. Kanał musi znajdować się
     * w kategori eventy. W innym przypadku zwraca false.
     */
    public boolean checkChannelIsInEventCategory(GuildMessageReceivedEvent event) {
        if (userHaveChannel(event.getMessage().getAuthor().getId(), event.getChannel())) {
            List<Category> categories = event.getGuild().getCategories();
            for (Category c : categories) {
                if (c.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_EVENT_ID)) {
                    List<TextChannel> textChannels = c.getTextChannels();
                    for (TextChannel tc : textChannels) {
                        if (tc.getId().equalsIgnoreCase(event.getChannel().getId())) {
                            return true;
                        }
                    }
                    break;
                }
            }
        } else {
            event.getMessage().delete().submit();
            EmbedInfo.cantChangeTitle(event.getAuthor().getId());
        }
        return false;
    }

    private boolean userHaveChannel(String userID, TextChannel channel) {
        if (!textChannelsUser.isEmpty()) {
            for (int i = 0; i < textChannelsUser.size(); i++) {
                if (textChannelsUser.containsKey(userID)) {
                    if (textChannelsUser.get(userID).getId().equalsIgnoreCase(channel.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<MemberMy> getMainList(int indexOfEvent) {
        return activeEvents.get(indexOfEvent).getMainList();
    }

    public List<MemberMy> getReserveList(int indexOfEvent) {
        return activeEvents.get(indexOfEvent).getReserveList();
    }

    /**
     * @param eventID ID wiadomości w której znajdują się zapisy
     * @return Zwraca ID kanalu na którym znajduje sie wiadomosc, w innym przypadku zwraca pustego Stringa
     */
    public String getChannelID(String eventID) {
        for (ActiveEvent ae : activeEvents) {
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
        privateChannel.sendMessage(activeEventsBuilder.build()).queue();
        for (ActiveEvent ae : activeEvents) {
            List<MemberMy> mainList = ae.getMainList();
            List<MemberMy> reserveList = ae.getReserveList();
            JDA jda = Repository.getJda();
            String channelName = jda.getTextChannelById(ae.getChannelID()).getName();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.WHITE);
            builder.addField("ID eventu", ae.getMessageID(), false);
            builder.addField("ID kanału", ae.getChannelID(), false);
            builder.addField("Nazwa kanału", channelName, false);
            builder.addField("Ilość zapisanych", String.valueOf(ae.getNumberOfSignIn()), true);
            builder.addField("Główna lista", String.valueOf(mainList.size()), true);
            builder.addField("Rezerwowa lista", String.valueOf(reserveList.size()), true);
            String stringMainList = "";
            for (MemberMy m : mainList) {
                stringMainList += m.getUserName() + ", ";
            }
            builder.addField("Główna lista", stringMainList, false);
            String stringReserveList = "";
            for (MemberMy m : reserveList) {
                stringReserveList += m.getUserName() + ", ";
            }
            builder.addField("Rezerwowa lista", stringReserveList, false);
            privateChannel.sendMessage(builder.build()).queue();
        }
    }

    public String getEventNameFromEmbed(String eventID) {
        int indexActiveEvent = getIndexActiveEvent(eventID);
        String channelID = activeEvents.get(indexActiveEvent).getChannelID();
        JDA jda = Repository.getJda();
        Message message = jda.getTextChannelById(channelID).retrieveMessageById(eventID).complete();
        List<MessageEmbed> embeds = message.getEmbeds();
        String title = embeds.get(0).getTitle();
        return title;
    }

    public String getDateAndTimeFromEmbed(String eventID) {
        int indexActiveEvent = getIndexActiveEvent(eventID);
        String channelID = activeEvents.get(indexActiveEvent).getChannelID();
        JDA jda = Repository.getJda();
        Message message = jda.getTextChannelById(channelID).retrieveMessageById(eventID).complete();
        List<MessageEmbed> embeds = message.getEmbeds();
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        String date = fields.get(0).getValue();
        String time = fields.get(2).getValue();
        return date + "r., " + time;
    }
}

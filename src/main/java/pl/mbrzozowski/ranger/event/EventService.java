package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.embed.EmbedSettings;
import pl.mbrzozowski.ranger.event.reminder.CreateReminder;
import pl.mbrzozowski.ranger.event.reminder.Timers;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.repository.main.EventRepository;
import pl.mbrzozowski.ranger.response.ResponseMessage;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

@Service
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final Timers timers;
    private final UsersReminderService usersReminderService;

    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);

    @Autowired
    public EventService(EventRepository eventRepository,
                        Timers timers,
                        UsersReminderService usersReminderService) {
        this.eventRepository = eventRepository;
        this.timers = timers;
        this.usersReminderService = usersReminderService;
        setReminders();
    }

    private void setReminders() {
        List<Event> eventList = findAll();
        List<Event> events = eventList
                .stream()
                .filter(event -> event.getDate().isAfter(LocalDateTime.now(ZoneId.of("Europe/Paris"))))
                .toList();
        for (Event event : events) {
            if (event.isActive()) {
                CreateReminder createReminder = new CreateReminder(event, this, timers, usersReminderService);
                createReminder.create();
            }
        }
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public void createNewEvent(final @NotNull EventRequest eventRequest) {
        String userName = Users.getUserNicknameFromID(eventRequest.getAuthorId());
        RangerLogger.info(userName + " - tworzy nowy event.");
        if (checkRequest(eventRequest)) {
            if (Validation.eventDateTimeAfterNow(eventRequest.getDate() + " " + eventRequest.getTime())) {
                createEventChannel(eventRequest);
            }
        } else {
            RangerLogger.info("Brak wymaganych parametrów -name <nazwa> -date <data> -time <czas>");
        }
    }

    private void createEventChannel(final EventRequest eventRequest) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            return;
        }
        String channelName = EmbedSettings.GREEN_CIRCLE + eventRequest.getName() +
                "-" + eventRequest.getDate() + "-" + eventRequest.getTime();
        if (channelName.length() >= 99) {
            channelName = channelName.substring(0, 99);
        }
        Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_EVENT_ID);
        if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER_ADN_RECRUIT || eventRequest.getEventFor() == EventFor.RECRUIT) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.RECRUT_ID), permissions, null)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        }
    }

    private void createList(final TextChannel textChannel, final @NotNull EventRequest eventRequest) {
        String msg = "";
        if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER_ADN_RECRUIT) {
            msg = "<@&" + "RoleID.CLAN_MEMBER_ID" + "> <@&" + "RoleID.RECRUT_ID" + "> Zapisy!";
        } else if (eventRequest.getEventFor() == EventFor.RECRUIT) {
            msg = "<@&" + "RoleID.RECRUT_ID" + "> Zapisy!";
        } else if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER) {
            msg = "<@&" + "RoleID.CLAN_MEMBER_ID" + "> Zapisy!";
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(eventRequest.getName());
        if (StringUtils.isNotBlank(eventRequest.getDescription())) {
            builder.setDescription(eventRequest.getDescription());
        }
        builder.addField(EmbedSettings.WHEN_DATE, eventRequest.getDate(), true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.WHEN_TIME, eventRequest.getTime(), true);
        builder.addBlankField(false);
        builder.addField(EmbedSettings.NAME_LIST + "(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.NAME_LIST_RESERVE + "(0)", ">>> -", true);
        builder.setFooter("Utworzony przez " + Users.getUserNicknameFromID(eventRequest.getAuthorId()));
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
                    LocalDateTime dateTime = getDateTime(eventRequest.getDate(), eventRequest.getTime());
                    Event event = Event.builder()
                            .name(eventRequest.getName())
                            .msgId(message.getId())
                            .channelId(textChannel.getId())
                            .isActive(true)
                            .date(dateTime)
                            .build();
                    save(event);
                    CreateReminder reminder = new CreateReminder(event, this, timers, usersReminderService);
                    reminder.create();
                });
    }

    private void save(Event event) {
        eventRepository.save(event);
    }

    private @Nullable LocalDateTime getDateTime(String date, String time) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        String dateTime = date + " " + time;
        try {
            return LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean checkRequest(@NotNull EventRequest eventRequest) {
        if (StringUtils.isBlank(eventRequest.getName())) {
            return false;
        }
        if (StringUtils.isBlank(eventRequest.getDate())) {
            return false;
        }
        return !StringUtils.isBlank(eventRequest.getTime());
    }

    public void updateEmbed(@NotNull Event event) {
        String channelID = event.getChannelId();
        String messageID = event.getMsgId();
        TextChannel channel = DiscordBot.getJda().getTextChannelById(channelID);
        if (channel != null) {
            channel.retrieveMessageById(messageID).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                MessageEmbed mOld = embeds.get(0);
                List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
                List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
                String mainList = getStringOfMainList(event);
                String reserveList = getStringOfReserveList(event);

                for (int i = 0; i < fieldsOld.size(); i++) {
                    if (i == 4) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(EmbedSettings.NAME_LIST + "(" + getMainListSize(event) + ")", ">>> " + mainList, true);
                        fieldsNew.add(fieldNew);
                    } else if (i == 6) {
                        MessageEmbed.Field fieldNew = new MessageEmbed.Field(EmbedSettings.NAME_LIST_RESERVE + "(" + getReserveListSize(event) + ")", ">>> " + reserveList, true);
                        fieldsNew.add(fieldNew);
                    } else {
                        fieldsNew.add(fieldsOld.get(i));
                    }
                }

                int color;
                if (getMainListSize(event) >= 9) {
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
    }

    private int getMainListSize(@NotNull Event event) {
        return event.getPlayers().stream().filter(Player::isMainList).toList().size();
    }

    private int getReserveListSize(@NotNull Event event) {
        return event.getPlayers().stream().filter(player -> !player.isMainList()).toList().size();
    }

    private @NotNull String getStringOfMainList(@NotNull Event event) {
        List<Player> players = event.getPlayers().stream().filter(Player::isMainList).toList();
        if (players.size() > 0) {
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                result.append(getUserNameWithoutRangers(Users.getUserNicknameFromID(player.getUserId()))).append("\n");
            }
            return result.toString();
        } else {
            return "-";
        }
    }

    private @NotNull String getStringOfReserveList(@NotNull Event event) {
        List<Player> players = event.getPlayers().stream().filter(player -> !player.isMainList()).toList();
        if (players.size() > 0) {
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                result.append(getUserNameWithoutRangers(Users.getUserNicknameFromID(player.getUserId()))).append("\n");
            }
            return result.toString();
        } else {
            return "-";
        }
    }

    public String getUserNameWithoutRangers(String userNickname) {
        String result = userNickname;
        if (result.matches("(.*)<rRangersPL>(.*)")) {
            result = result.replace("<rRangersPL>", "");
        } else if (result.matches("(.*)<RangersPL>(.*)")) {
            result = result.replace("<RangersPL>", "");
        }
        return result;
    }


    public void buttonClick(@NotNull ButtonInteractionEvent buttonInteractionEvent, @NotNull Event event, ButtonClickType buttonClick) {
        String userName = Users.getUserNicknameFromID(buttonInteractionEvent.getUser().getId());
        String userID = buttonInteractionEvent.getUser().getId();
        if (eventIsAfter(event.getDate())) {
            switch (buttonClick) {
                case SIGN_IN -> {
                    Player player = getPlayer(event.getPlayers(), userID);
                    if (player != null) {
                        if (!player.isMainList()) {
                            player.setMainList(true);
                            buttonInteractionEvent.deferEdit().queue();
                            RangerLogger.info(Users.getUserNicknameFromID(userID) + " przepisał się na listę.", event.getName());
                        } else {
                            ResponseMessage.youAreOnList(buttonInteractionEvent);
                        }
                    } else {
                        Player newPlayer = new Player(null, userID, userName, true, event);
                        event.getPlayers().add(newPlayer);
                        buttonInteractionEvent.deferEdit().queue();
                        RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę.", event.getName());
                    }
                    eventRepository.save(event);
                }
                case SIGN_IN_RESERVE -> {
                    Player player = getPlayer(event.getPlayers(), userID);
                    if (player != null) {
                        if (player.isMainList()) {
                            if (threeHoursToEvent(event.getDate())) {
                                ResponseMessage.youCantSignReserve(buttonInteractionEvent);
                                RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z głownej listy na rezerwową ["
                                        + event.getName() + "] - Czas do eventu 3h lub mniej.");
                            } else {
                                player.setMainList(false);
                                buttonInteractionEvent.deferEdit().queue();
                                RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę rezerwową.", event.getName());
                            }
                        } else {
                            ResponseMessage.youAreOnList(buttonInteractionEvent);
                        }
                    } else {
                        Player newPlayer = new Player(null, userID, userName, false, event);
                        event.getPlayers().add(newPlayer);
                        buttonInteractionEvent.deferEdit().queue();
                        RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę rezerwową.", event.getName());
                    }
                    eventRepository.save(event);
                }
                case SIGN_OUT -> {
                    Player player = getPlayer(event.getPlayers(), userID);
                    if (player != null) {
                        if (threeHoursToEvent(event.getDate())) {
                            ResponseMessage.youCantSingOut(buttonInteractionEvent);
                            RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z eventu ["
                                    + event.getName() + "] - Czas do eventu 3h lub mniej.");
                        } else {
                            event.getPlayers().removeIf(p -> p.getUserId().equalsIgnoreCase(userID));
                            buttonInteractionEvent.deferEdit().queue();
                        }
                    } else {
                        ResponseMessage.youAreNotOnList(buttonInteractionEvent);
                    }
                    eventRepository.save(event);
                }
            }
        } else {
            RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] Kliknął w przycisk ["
                    + event.getName() + "] - Event się już rozpoczął.");
            ResponseMessage.eventIsBefore(buttonInteractionEvent);
            disableButtons(event);
            changeTitleRedCircle(event);
        }
        updateEmbed(event);
    }

    private @Nullable Player getPlayer(@NotNull List<Player> players, String userID) {
        for (Player player : players) {
            if (player.getUserId().equalsIgnoreCase(userID)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Sprawdza czy event już się wydarzył.
     *
     * @return Zwraca true jeśli event się jeszcze nie wydarzył. W innym przypadku zwraca false.
     */
    private boolean eventIsAfter(@NotNull LocalDateTime dateEvent) {
        LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        return dateEvent.isAfter(dateNow);
    }

    /**
     * Sprawdza czy pozostały trzy godziny do eventu
     *
     * @return Zwraca true jeżeli pozostały trzy godziny lub mniej do eventu, w innym przypadku zwraca false
     */
    private boolean threeHoursToEvent(LocalDateTime eventTime) {
        eventTime = eventTime.minusHours(3);
        LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        return dateNow.isAfter(eventTime);
    }

    public void delete(Event event) {
        eventRepository.delete(event);
    }

    public void cancelEvent(Event event, boolean sendNotifi) {
        disableButtons(event);
        changeTitleRedCircle(event);
        event.setActive(false);
        timers.cancelByMsgId(event.getMsgId());
        save(event);
        if (sendNotifi) {
            String dateTime = "<t:" + event.getDate().atZone(ZoneId.of("Europe/Paris")).toEpochSecond() + ":F>";
            sendInfoChanges(event, EventChanges.REMOVE, dateTime);
        }
    }

    public void changeTitleRedCircle(@NotNull Event event) {
        TextChannel channel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (channel != null) {
            String buffor = channel.getName();
            buffor = buffor.replace(EmbedSettings.GREEN_CIRCLE, EmbedSettings.RED_CIRCLE);
            channel.getManager()
                    .setName(buffor)
                    .queue();
        }
    }

    public void changeDateAndTime(@NotNull Event event, boolean notifi) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        assert textChannel != null;
        textChannel.retrieveMessageById(event.getMsgId()).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed mOld = embeds.get(0);
            List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
            String month;
            if (event.getDate().getMonthValue() < 10) {
                month = "0" + event.getDate().getMonthValue();
            } else {
                month = String.valueOf(event.getDate().getMonthValue());
            }
            String newDate = event.getDate().getDayOfMonth() + "." + month + "." + event.getDate().getYear();
            String min;
            if (event.getDate().getMinute() < 10) {
                min = "0" + event.getDate().getMinute();
            } else {
                min = String.valueOf(event.getDate().getMinute());
            }
            String newTime = event.getDate().getHour() + ":" + min;
            String dataTime = newDate + " " + newTime;

            textChannel.getManager().setName(EmbedSettings.GREEN_CIRCLE + event.getName() + dataTime).queue();
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
                updateTimer(event);
                if (notifi) {
                    String dateTime = "<t:" + event.getDate().atZone(ZoneId.of("Europe/Paris")).toEpochSecond() + ":F>";
                    sendInfoChanges(event, EventChanges.CHANGES, dateTime);
                }
            });
        });
        eventRepository.save(event);
    }

    public void sendInfoChanges(Event event, EventChanges whatChange, String dateTime) {
        List<Player> mainList = getMainList(event);
        List<Player> reserveList = getReserveList(event);
        RangerLogger.info(
                "Zapisanych na glównej liście: [" + mainList.size() + "], Rezerwa: [" + reserveList.size() + "] - Wysyłam informację.",
                event.getMsgId());
        for (Player player : mainList) {
            EmbedInfo.sendInfoChanges(player.getUserId(), event, whatChange, dateTime);
        }
        for (Player player : reserveList) {
            EmbedInfo.sendInfoChanges(player.getUserId(), event, whatChange, dateTime);
        }
    }

    private void updateTimer(@NotNull Event event) {
        timers.cancelByMsgId(event.getMsgId());
        CreateReminder reminder = new CreateReminder(event, this, timers, usersReminderService);
        reminder.create();
    }

    /**
     * Wyłącza przyciski w zapisach
     */
    public void disableButtons(@NotNull Event event) {
        disableButtons(event.getMsgId(), event.getChannelId());
    }

    public void disableButtons(String messageId) {
        Optional<Event> eventOptional = findEventByMsgId(messageId);
        eventOptional.ifPresent(this::disableButtons);
    }

    /**
     * @param messageID ID wiadomości eventu
     * @param channelID ID kanału na którym znajduję sie event
     */
    public void disableButtons(String messageID, String channelID) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelID);
        if (textChannel != null) {
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
    }

    public void enableButtons(String messageID) {
        Optional<Event> eventOptional = findEventByMsgId(messageID);
        eventOptional.ifPresent(event -> enableButtons(event.getMsgId(), event.getChannelId()));
    }

    public void enableButtons(String messageID, String channelID) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelID);
        assert textChannel != null;
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

    public List<Player> getMainList(@NotNull Event event) {
        return event.getPlayers().stream().filter(Player::isMainList).toList();
    }

    public List<Player> getReserveList(@NotNull Event event) {
        return event.getPlayers().stream().filter(player -> !player.isMainList()).toList();
    }

    private @NotNull String getDateAndTime(@NotNull List<MessageEmbed.Field> fields) {
        String date = fields.get(0).getValue();
        String time = fields.get(2).getValue();
        return date + "r., " + time;
    }


    public boolean isActiveEvents() {
        return eventRepository.findAll().size() > 0;
    }

    public Optional<Event> findEventByMsgId(String id) {
        return eventRepository.findByMsgId(id);
    }

    public void deleteByMsgId(String messageId) {
        timers.cancelByMsgId(messageId);
        eventRepository.deleteByMsgId(messageId);
    }

    public void deleteByChannelId(String channelId) {
        timers.cancelByChannelId(channelId);
        eventRepository.deleteByChannelId(channelId);
    }

    public void setActive(Event event, boolean isActive) {
        event.setActive(isActive);
        eventRepository.save(event);
    }
}

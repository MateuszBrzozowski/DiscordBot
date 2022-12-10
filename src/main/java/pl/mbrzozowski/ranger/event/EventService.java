package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.event.reminder.CreateReminder;
import pl.mbrzozowski.ranger.event.reminder.Timers;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.repository.main.EventRepository;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.EmbedSettings;
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
        log.info(userName + " - creating new event.");
        if (checkRequest(eventRequest)) {
            if (Validation.eventDateTimeAfterNow(eventRequest.getDate() + " " + eventRequest.getTime())) {
                createEventChannel(eventRequest);
            }
        } else {
            log.info("Brak wymaganych parametrów -name <nazwa> -date <data> -time <czas>");
        }
    }

    private void createEventChannel(final EventRequest eventRequest) {
        log.info("Creating new channel");
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            throw new NullPointerException("Guild by RangersPL is null");
        }
        String channelName = getStringChanelName(eventRequest);
        Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_EVENT_ID);
        if (category == null) {
            throw new NullPointerException("Category by Event Id is null");
        }
        createEventChannel(eventRequest, guild, channelName, category);
    }

    private void createEventChannel(@NotNull final EventRequest eventRequest,
                                    @NotNull final Guild guild,
                                    @NotNull final String channelName,
                                    @NotNull final Category category) {
        if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER_AND_RECRUIT || eventRequest.getEventFor() == EventFor.RECRUIT) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.RECRUIT_ID), permissions, null)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else if (eventRequest.getEventFor() == EventFor.TACTICAL_GROUP) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.TACTICAL_GROUP), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        }
    }

    @NotNull
    private String getStringChanelName(@NotNull EventRequest eventRequest) {
        String result = "";
        if (eventRequest.getEventFor() == EventFor.TACTICAL_GROUP) {
            result += EmbedSettings.BRAIN_WITH_GREEN;
        } else {
            result += EmbedSettings.GREEN_CIRCLE;
        }
        result += eventRequest.getName() +
                "-" + eventRequest.getDate() + "-" + eventRequest.getTime();
        if (result.length() >= 99) {
            result = result.substring(0, 99);
        }
        return result;
    }

    private void createList(final TextChannel textChannel, final @NotNull EventRequest eventRequest) {
        log.info("Creating list");
        String msg = getMessageForEventList(eventRequest);
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
                            .eventFor(eventRequest.getEventFor())
                            .build();
                    save(event);
                    CreateReminder reminder = new CreateReminder(event, this, timers, usersReminderService);
                    reminder.create();
                });
    }

    @NotNull
    private String getMessageForEventList(@NotNull EventRequest eventRequest) {
        String result = "";
        if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER_AND_RECRUIT) {
            result = "<@&" + RoleID.CLAN_MEMBER_ID + "> <@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor() == EventFor.RECRUIT) {
            result = "<@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER) {
            result = "<@&" + RoleID.CLAN_MEMBER_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor() == EventFor.TACTICAL_GROUP) {
            result = "<@&" + RoleID.TACTICAL_GROUP + "> Tactical meeting!";
        }
        return result;
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
        log.info("Event " + event.getName() + " updating embed");
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
        List<Player> players = new ArrayList<>(event.getPlayers().stream().filter(Player::isMainList).toList());
        if (players.size() > 0) {
            players.sort(Comparator.comparing(Player::getTimestamp));
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                String nickname = Users.getUserNicknameFromID(player.getUserId());
                nickname = prepareNicknameToEventList(nickname);
                result.append(nickname).append("\n");
            }
            return result.toString();
        } else {
            return "-";
        }
    }

    private @NotNull String getStringOfReserveList(@NotNull Event event) {
        List<Player> players = new ArrayList<>(event.getPlayers().stream().filter(player -> !player.isMainList()).toList());
        if (players.size() > 0) {
            players.sort(Comparator.comparing(Player::getTimestamp));
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                String nickname = Users.getUserNicknameFromID(player.getUserId());
                nickname = prepareNicknameToEventList(nickname);
                result.append(nickname).append("\n");
            }
            return result.toString();
        } else {
            return "-";
        }
    }

    private String prepareNicknameToEventList(String source) {
        source = StringModify.removeClanTag(source);
        source = StringModify.removeDiscordMarkdowns(source);
        return source;
    }

    public void buttonClick(@NotNull ButtonInteractionEvent buttonInteractionEvent, ButtonClickType buttonClick) {
        log.info("Event button click - " + buttonInteractionEvent.getUser().getName());
        Optional<Event> eventOptional = findEventByMsgId(buttonInteractionEvent.getMessage().getId());
        if (eventOptional.isEmpty()) {
            ResponseMessage.operationNotPossible(buttonInteractionEvent);
            return;
        }
        Event event = eventOptional.get();
        String userName = Users.getUserNicknameFromID(buttonInteractionEvent.getUser().getId());
        String userID = buttonInteractionEvent.getUser().getId();
        if (eventIsAfter(event.getDate())) {
            switch (buttonClick) {
                case SIGN_IN -> signIn(buttonInteractionEvent, event, userName, userID);
                case SIGN_IN_RESERVE -> signInReserve(buttonInteractionEvent, event, userName, userID);
                case SIGN_OUT -> signOut(buttonInteractionEvent, event, userID);
            }
        } else {
            RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] Kliknął w przycisk ["
                    + event.getName() + event.getDate().toString() + "] - Event się już rozpoczął.");
            ResponseMessage.eventIsBefore(buttonInteractionEvent);
            disableButtons(event);
            setRedCircleInChannelName(event);
        }
        updateEmbed(event);
    }

    private void signIn(@NotNull ButtonInteractionEvent buttonInteractionEvent, @NotNull Event event, String userName, String userID) {
        log.info(userName + " sign in");
        Player player = getPlayer(event.getPlayers(), userID);
        if (player != null) {
            if (!player.isMainList()) {
                player.setMainList(true);
                player.setTimestamp(LocalDateTime.now());
                buttonInteractionEvent.deferEdit().queue();
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " przepisał się na listę.", event.getName() + event.getDate().toString());
            } else {
                ResponseMessage.youAreOnList(buttonInteractionEvent);
            }
        } else {
            Player newPlayer = new Player(null, userID, userName, true, event, LocalDateTime.now());
            event.getPlayers().add(newPlayer);
            buttonInteractionEvent.deferEdit().queue();
            RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę.", event.getName() + event.getDate().toString());
        }
        eventRepository.save(event);
    }

    private void signInReserve(@NotNull ButtonInteractionEvent buttonInteractionEvent, @NotNull Event event, String userName, String userID) {
        log.info(userName + " sign in reserve");
        Player player = getPlayer(event.getPlayers(), userID);
        if (player != null) {
            if (player.isMainList()) {
                if (threeHoursToEvent(event.getDate())) {
                    ResponseMessage.youCantSignReserve(buttonInteractionEvent);
                    RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z głównej listy na rezerwową ["
                            + event.getName() + event.getDate().toString() + "] - Czas do eventu 3h lub mniej.");
                } else {
                    player.setMainList(false);
                    player.setTimestamp(LocalDateTime.now());
                    buttonInteractionEvent.deferEdit().queue();
                    RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę rezerwową.", event.getName() + event.getDate().toString());
                }
            } else {
                ResponseMessage.youAreOnList(buttonInteractionEvent);
            }
        } else {
            Player newPlayer = new Player(null, userID, userName, false, event, LocalDateTime.now());
            event.getPlayers().add(newPlayer);
            buttonInteractionEvent.deferEdit().queue();
            RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę rezerwową.", event.getName() + event.getDate().toString());
        }
        eventRepository.save(event);
    }

    private void signOut(@NotNull ButtonInteractionEvent buttonInteractionEvent, @NotNull Event event, String userID) {
        log.info(userID + " sign out");
        Player player = getPlayer(event.getPlayers(), userID);
        if (player != null) {
            if (threeHoursToEvent(event.getDate())) {
                ResponseMessage.youCantSingOut(buttonInteractionEvent);
                RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z eventu ["
                        + event.getName() + event.getDate().toString() + "] - Czas do eventu 3h lub mniej.");
            } else {
                event.getPlayers().removeIf(p -> p.getUserId().equalsIgnoreCase(userID));
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " wypisał się z eventu", event.getName() + event.getDate().toString());
                buttonInteractionEvent.deferEdit().queue();
            }
        } else {
            ResponseMessage.youAreNotOnList(buttonInteractionEvent);
        }
        eventRepository.save(event);
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
        log.info(event.getName() + " cancel event");
        disableButtons(event);
        setRedCircleInChannelName(event);
        event.setActive(false);
        timers.cancelByMsgId(event.getMsgId());
        save(event);
        if (sendNotifi) {
            String dateTime = "<t:" + event.getDate().atZone(ZoneId.of("Europe/Paris")).toEpochSecond() + ":F>";
            sendInfoChanges(event, EventChanges.REMOVE, dateTime);
        }
    }

    public void setRedCircleInChannelName(@NotNull Event event) {
        TextChannel channel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (channel != null) {
            String buffer = channel.getName();
            buffer = removeAnyPrefixCircle(buffer);
            channel.getManager()
                    .setName(EmbedSettings.RED_CIRCLE + buffer)
                    .queue();
        }
    }

    public void updateEmbed(@NotNull Event event,
                            boolean isChangedDateTime,
                            boolean isChangedName,
                            boolean isChangedDescription,
                            String description,
                            boolean notifi) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (textChannel == null) {
            return;
        }
        textChannel.retrieveMessageById(event.getMsgId()).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed mOld = embeds.get(0);
            List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>(fieldsOld.stream().toList());
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

            if (isChangedDateTime || isChangedName) {
                updateChannelName(event, textChannel, dataTime);
            }

            if (isChangedDateTime) {
                fieldsNew.clear();
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
            }
            String newTitle = mOld.getTitle();
            if (isChangedName) {
                newTitle = event.getName();
            }
            String newDescription = mOld.getDescription();
            if (isChangedDescription) {
                newDescription = description;
            }
            MessageEmbed mNew = new MessageEmbed(mOld.getUrl()
                    , newTitle
                    , newDescription
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
        save(event);
    }

    private void updateChannelName(@NotNull Event event, @NotNull TextChannel textChannel, String dataTime) {
        textChannel
                .getManager()
                .setName(EmbedSettings.GREEN_CIRCLE + event.getName() + dataTime)
                .queue();
    }

    public void sendInfoChanges(Event event, EventChanges whatChange, String dateTime) {
        List<Player> mainList = getMainList(event);
        List<Player> reserveList = getReserveList(event);
        log.info("Run reminder: Main list - [" + mainList.size() + "], Reserve - [" + reserveList.size() + "]");
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

    public boolean isActiveEvents() {
        return eventRepository.findAll().size() > 0;
    }

    public Optional<Event> findEventByMsgId(String id) {
        return eventRepository.findByMsgId(id);
    }

    public void deleteByMsgId(String messageId) {
        log.info("Event deleting by msg id");
        timers.cancelByMsgId(messageId);
        eventRepository.deleteByMsgId(messageId);
    }

    public void deleteByChannelId(String channelId) {
        log.info("Event deleting by channel id");
        timers.cancelByChannelId(channelId);
        eventRepository.deleteByChannelId(channelId);
    }

    public void setActive(Event event, boolean isActive) {
        event.setActive(isActive);
        eventRepository.save(event);
    }

    public void setYellowCircleInChannelName(String channelId, EventFor eventFor) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelId);
        if (textChannel == null) {
            return;
        }
        String channelName = textChannel.getName();
        channelName = removeAnyPrefixCircle(channelName);
        if (eventFor == EventFor.TACTICAL_GROUP) {
            channelName = EmbedSettings.BRAIN_WITH_YELLOW + channelName;
        } else {
            channelName = EmbedSettings.YELLOW_CIRCLE + channelName;
        }
        textChannel.getManager().setName(channelName).queue();
    }

    @NotNull
    private String removeAnyPrefixCircle(@NotNull String channelName) {
        if (channelName.contains(EmbedSettings.BRAIN_WITH_GREEN)) {
            channelName = channelName.replaceAll(EmbedSettings.BRAIN_WITH_GREEN, "");
        }
        if (channelName.contains(EmbedSettings.BRAIN_WITH_YELLOW)) {
            channelName = channelName.replaceAll(EmbedSettings.BRAIN_WITH_YELLOW, "");
        }
        if (channelName.contains(EmbedSettings.YELLOW_CIRCLE)) {
            channelName = channelName.replaceAll(EmbedSettings.YELLOW_CIRCLE, "");
        }
        if (channelName.contains(EmbedSettings.RED_CIRCLE)) {
            channelName = channelName.replaceAll(EmbedSettings.RED_CIRCLE, "");
        }
        if (channelName.contains(EmbedSettings.GREEN_CIRCLE)) {
            channelName = channelName.replaceAll(EmbedSettings.GREEN_CIRCLE, "");
        }
        return channelName;
    }
}

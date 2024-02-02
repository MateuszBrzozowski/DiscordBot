package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
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
import pl.mbrzozowski.ranger.exceptions.FullListException;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.repository.main.EventRepository;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class EventService {

    private static final int MAX_EVENTS = 25;
    private final UsersReminderService usersReminderService;
    private final SettingsService settingsService;
    private final EventRepository eventRepository;
    private final Timers timers;

    @Autowired
    public EventService(UsersReminderService usersReminderService,
                        EventRepository eventRepository,
                        SettingsService settingsService,
                        Timers timers) {
        this.eventRepository = eventRepository;
        this.settingsService = settingsService;
        this.timers = timers;
        this.usersReminderService = usersReminderService;
        setReminders();
    }

    private void save(Event event) {
        eventRepository.save(event);
    }

    void delete(Event event) {
        eventRepository.delete(event);
    }

    List<Event> findAll() {
        return eventRepository.findAll();
    }

    List<Event> findByIsActive() {
        return eventRepository.findByIsActive(true);
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
        log.info("Event deleted by messageId(messageId={})", messageId);
    }

    public void deleteByChannelId(String channelId) {
        timers.cancelByChannelId(channelId);
        eventRepository.deleteByChannelId(channelId);
        log.info("Event deleted by channelId(channelId={})", channelId);
    }

    void setActiveToFalse(@NotNull Event event) {
        event.setActive(false);
        eventRepository.save(event);
    }

    public List<Player> getMainList(@NotNull Event event) {
        return event.getPlayers().stream().toList();
    }

    private void setReminders() {
        List<Event> eventList = findAll();
        List<Event> events = filterEventsAfterNow(eventList);
        for (Event event : events) {
            if (event.isActive()) {
                CreateReminder createReminder = new CreateReminder(event, this, timers, usersReminderService);
                createReminder.create();
            }
        }
    }

    @NotNull
    private List<Event> filterEventsAfterNow(@NotNull List<Event> eventList) {
        return eventList
                .stream()
                .filter(event -> event.getDate() != null)
                .filter(event -> event.getDate()
                        .isAfter(LocalDateTime.now(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS))))
                .toList();
    }

    void createNewEvent(@NotNull final EventRequest eventRequest) {
        log.info(Users.getUserNicknameFromID(eventRequest.getAuthorId()) + " - creating new event.");
        Validator.isValidEventRequest(eventRequest);
        createEventChannel(eventRequest);
    }

    private void createEventChannel(final EventRequest eventRequest) {
        log.info("Creating new channel");
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            throw new NullPointerException("Guild by RangersPL is null");
        }
        Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_EVENT_ID);
        if (category == null) {
            throw new NullPointerException("Category by Event Id is null");
        }
        String channelName = StringProvider.getChannelName(eventRequest);
        createEventChannel(eventRequest, guild, channelName, category);
    }

    private void createEventChannel(@NotNull final EventRequest eventRequest,
                                    @NotNull final Guild guild,
                                    @NotNull final String channelName,
                                    @NotNull final Category category) {
        final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_MENTION_EVERYONE);
        if (eventRequest.getEventFor().equals(EventFor.CLAN_MEMBER_AND_RECRUIT) || eventRequest.getEventFor().equals(EventFor.RECRUIT)) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.RECRUIT_ID), permissions, null)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else if (eventRequest.getEventFor().equals(EventFor.SQ_EVENTS)) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .addRolePermissionOverride(Long.parseLong(RoleID.RECRUIT_ID), permissions, null)
                    .addRolePermissionOverride(Long.parseLong(RoleID.SQ_EVENTS), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else if (eventRequest.getEventFor().equals(EventFor.TACTICAL_GROUP)) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.TACTICAL_GROUP), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else if (eventRequest.getEventFor().equals(EventFor.CLAN_COUNCIL)) {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_COUNCIL), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        } else {
            guild.createTextChannel(channelName, category)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions)
                    .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permissions, null)
                    .queue(textChannel -> createList(textChannel, eventRequest));
        }
    }


    private void createList(@NotNull final TextChannel textChannel, @NotNull final EventRequest eventRequest) {
        log.info("Creating list");
        String msg = StringProvider.getMessageForEventList(eventRequest);
        EmbedBuilder builder = EventsEmbed.getEventEmbedBuilder(eventRequest);
        textChannel.sendMessage(msg).setEmbeds(builder.build()).queue(message -> {
            MessageEmbed mOld = message.getEmbeds().get(0);
            String msgId = message.getId();
            message.editMessageEmbeds(mOld).setActionRow(EventsEmbed.getActionRowForEvent(msgId)).queue();
            message.pin().queue();
            createEvent(eventRequest, textChannel.getId(), message.getId());
        });
    }

    private void createEvent(@NotNull EventRequest eventRequest, @NotNull String channelId, @NotNull String messageId) {
        Event event = Event
                .builder()
                .name(eventRequest.getName())
                .msgId(messageId)
                .channelId(channelId)
                .isActive(true)
                .date(eventRequest.getDateTime())
                .eventFor(eventRequest.getEventFor()).build();
        save(event);
        CreateReminder reminder = new CreateReminder(event, this, timers, usersReminderService);
        reminder.create();
    }

    public void buttonClick(@NotNull ButtonInteractionEvent buttonInteractionEvent, @NotNull ButtonClickType buttonClick) {
        log.info(buttonInteractionEvent.getUser() + " - Event, button type: " + buttonClick);
        Optional<Event> eventOptional = findEventByMsgId(buttonInteractionEvent.getMessage().getId());
        if (eventOptional.isEmpty()) {
            ResponseMessage.operationNotPossible(buttonInteractionEvent);
            return;
        }
        Event event = eventOptional.get();
        String userName = Users.getUserNicknameFromID(buttonInteractionEvent.getUser().getId());
        String userID = buttonInteractionEvent.getUser().getId();
        boolean isSuccess = false;
        if (Validator.isDateTimeAfterNow(event.getDate())) {
            switch (buttonClick) {
                case SIGN_IN -> isSuccess = signIn(buttonInteractionEvent, event, userName, userID);
                case SIGN_OUT -> isSuccess = signOut(buttonInteractionEvent, event, userID);
            }
        } else {
            ResponseMessage.eventIsBefore(buttonInteractionEvent);
            disableButtons(event);
            setRedCircleInChannelName(event);
        }
        if (isSuccess) {
            buttonInteractionEvent.deferEdit().queue();
        } else {
            return;
        }
        try {
            updateEmbed(event);
            eventRepository.save(event);
        } catch (FullListException e) {
            ResponseMessage.listIsFull(buttonInteractionEvent);
        }
    }

    private boolean signIn(@NotNull ButtonInteractionEvent buttonInteractionEvent,
                           @NotNull Event event,
                           String userName,
                           String userID) {
        log.info(userName + " sign in");
        Player player = getPlayer(event.getPlayers(), userID);
        if (player != null) {
            ResponseMessage.youAreOnList(buttonInteractionEvent);
            return false;
        } else {
            Player newPlayer = new Player(null, userID, userName, true, event, LocalDateTime.now());
            event.getPlayers().add(newPlayer);
            RangerLogger.info(Users.getUserNicknameFromID(userID) + " zapisał się na listę.", event.getName() + event.getDate().toString());
            return true;
        }
    }

    private boolean signOut(@NotNull ButtonInteractionEvent buttonInteractionEvent, @NotNull Event event, String userID) {
        log.info(userID + " sign out");
        Player player = getPlayer(event.getPlayers(), userID);
        if (player != null) {
            if (Validator.isThreeHoursToEvent(event.getDate())) {
                ResponseMessage.youCantSingOut(buttonInteractionEvent);
                RangerLogger.info("[" + Users.getUserNicknameFromID(userID) + "] chciał wypisać się z eventu [" + event.getName() + event.getDate().toString() + "] - Czas do eventu 3h lub mniej.");
                return false;
            } else {
                event.getPlayers().removeIf(p -> p.getUserId().equalsIgnoreCase(userID));
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " wypisał się z eventu", event.getName() + event.getDate().toString());
                return true;
            }
        } else {
            ResponseMessage.youAreNotOnList(buttonInteractionEvent);
            return false;
        }
    }

    /**
     * Searching player with id on list. If player exist, return this player. If not return null.
     *
     * @param players List of {@link Player}
     * @param userID  String with user ID
     * @return player with userID or null
     */
    private @Nullable Player getPlayer(@NotNull List<Player> players, String userID) {
        for (Player player : players) {
            if (player.getUserId().equalsIgnoreCase(userID)) {
                return player;
            }
        }
        return null;
    }

    void cancelEvent(@NotNull Event event, boolean sendNotifi) {
        log.info(event.getName() + " cancel event");
        setEmbedToCancel(event);
        setRedCircleInChannelName(event);
        event.setActive(false);
        timers.cancelByMsgId(event.getMsgId());
        save(event);
        if (sendNotifi) {
            String dateTime = Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate());
            sendInfoChanges(event, EventChanges.REMOVE, dateTime);
        }
    }

    private void setEmbedToCancel(@NotNull Event event) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (textChannel == null) {
            return;
        }
        textChannel.retrieveMessageById(event.getMsgId()).queue(message -> {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            String description = "## Odwołany\n";
            if (StringUtils.isNotBlank(messageEmbed.getDescription())) {
                description += messageEmbed.getDescription();
            }
            EmbedBuilder builder = new EmbedBuilder(messageEmbed);
            builder.setDescription(description);
            message.editMessageEmbeds(builder.build()).setComponents(ActionRow.of(
                    Button.primary("ID", "Zapisz").asDisabled(),
                    Button.danger("ID2", "Wypisz").asDisabled()
            )).queue();
        });
    }

    void setRedCircleInChannelName(@NotNull Event event) {
        TextChannel channel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (channel != null) {
            String buffer = channel.getName();
            buffer = StringProvider.removeAnyPrefixCircle(buffer);
            channel.getManager().setName(EmbedSettings.RED_CIRCLE + buffer).queue();
        }
    }

    void updateEmbed(@NotNull Event event) throws FullListException {
        log.info("Event " + event.getName() + " updating embed");
        String mainList = EventsEmbed.getStringOfMainList(event);
        String channelID = event.getChannelId();
        String messageID = event.getMsgId();
        TextChannel channel = DiscordBot.getJda().getTextChannelById(channelID);
        if (channel != null) {
            channel.retrieveMessageById(messageID).queue(message -> {
                MessageEmbed messageEmbed = EventsEmbed.getMessageEmbedWithUpdatedLists(event, message, mainList);
                message.editMessageEmbeds(messageEmbed).setSuppressEmbeds(false).queue();
                log.info("Embed updated");
            });
        }
    }

    void updateEmbed(@NotNull Event event,
                     boolean isChangedDateTime,
                     boolean isChangedName,
                     boolean isChangedDescription,
                     String description,
                     boolean notifi) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (textChannel == null) {
            return;
        }
        if (isChangedDateTime || isChangedName) {
            updateChannelName(event, textChannel);
        }
        textChannel.retrieveMessageById(event.getMsgId()).queue(message -> {
            MessageEmbed mNew = EventsEmbed.getUpdatedEmbed(event, message, isChangedDateTime, isChangedName, isChangedDescription, description);
            message.editMessageEmbeds(mNew).queue();
        });
        updateTimer(event);
        if (notifi) {
            String dateTime = Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate());
            sendInfoChanges(event, EventChanges.CHANGES, dateTime);
        }
        save(event);
    }

    private void updateChannelName(@NotNull Event event, @NotNull TextChannel textChannel) {
        String newName = StringProvider.getChannelName(event);
        textChannel.getManager().setName(newName).queue();
    }

    private void sendInfoChanges(Event event, EventChanges whatChange, String dateTime) {
        List<Player> mainList = getMainList(event);
        log.info("Run reminder: Main list - [" + mainList.size() + "]");
        for (Player player : mainList) {
            EmbedInfo.sendInfoChanges(player.getUserId(), event, whatChange, dateTime);
        }
    }

    private void updateTimer(@NotNull Event event) {
        timers.cancelByMsgId(event.getMsgId());
        CreateReminder reminder = new CreateReminder(event, this, timers, usersReminderService);
        reminder.create();
    }

    public void disableButtons(@NotNull Event event) {
        disableButtons(event.getMsgId(), event.getChannelId());
    }

    public void disableButtons(String messageId) {
        Optional<Event> eventOptional = findEventByMsgId(messageId);
        eventOptional.ifPresent(this::disableButtons);
    }

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
                message.editMessageEmbeds(embeds.get(0)).setActionRow(buttonsNew).queue();
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
            message.editMessageEmbeds(embeds.get(0)).setActionRow(buttonsNew).queue();
        });
    }

    public void setYellowCircleInChannelName(String channelId, EventFor eventFor) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelId);
        if (textChannel == null) {
            return;
        }
        String channelName = textChannel.getName();
        channelName = StringProvider.removeAnyPrefixCircle(channelName);
        channelName = StringProvider.addYellowCircleBeforeText(channelName, eventFor);
        textChannel.getManager().setName(channelName).queue();
    }

    public void fixEmbed(@NotNull SlashCommandInteractionEvent event) {
        String messageId = Objects.requireNonNull(event.getOption("id")).getAsString();
        Optional<Event> eventOptional = findEventByMsgId(messageId);
        if (eventOptional.isPresent()) {
            event.reply("Naprawiam listę").setEphemeral(true).queue();
            try {
                updateEmbed(eventOptional.get());
            } catch (FullListException e) {
                throw new RuntimeException(e);
            }
        } else {
            event.reply("Event o podanym id nie istnieje").setEphemeral(true).queue();
        }
    }

    public boolean isMaxEvents() {
        List<Event> all = findAll();
        return all.size() >= MAX_EVENTS;
    }
}

package pl.mbrzozowski.ranger.event.reminder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.embed.EmbedSettings;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.Player;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.RangerLogger;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;

public class Reminder extends TimerTask {

    private final String eventID;
    private final TypeOfReminder typeOfReminder;
    private final EventService eventService;
    private final UsersReminderService usersReminderService;

    public Reminder(String eventID,
                    TypeOfReminder type,
                    EventService eventService,
                    UsersReminderService usersReminderService) {
        this.eventID = eventID;
        this.typeOfReminder = type;
        this.eventService = eventService;
        this.usersReminderService = usersReminderService;
    }

    @Override
    public void run() {
        Optional<Event> eventOptional = eventService.findEventByMsgId(eventID);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            List<Player> mainList = eventService.getMainList(event);
            List<Player> reserveList = eventService.getReserveList(event);
            List<UsersReminder> usersReminderList = usersReminderService.findAll();
            RangerLogger.info("Zapisanych na głównej liście: [" + mainList.size() + "], Rezerwa: [" +
                    reserveList.size() + "] - Wysyłam przypomnienia.", event.getName());
            String linkToEvent = "[" + event.getName() + "](https://discord.com/channels/" +
                    CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" + event.getChannelId() + "/" + eventID + ")";
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
            String dateTimeEvent = event.getDate().format(dateFormat);
            for (Player player : mainList) {
                if (userHasOn(usersReminderList, player.getUserId())) {
                    sendMessage(player.getUserId(), linkToEvent, dateTimeEvent);
                }
            }
            for (Player player : reserveList) {
                if (userHasOn(usersReminderList, player.getUserId())) {
                    sendMessage(player.getUserId(), linkToEvent, dateTimeEvent);
                }
            }
        }
    }

    private boolean userHasOn(@NotNull List<UsersReminder> list, String userId) {
        int size = list.stream()
                .filter(usersReminder -> usersReminder.getUserId().equalsIgnoreCase(userId))
                .toList()
                .size();
        return size == 0;
    }

    private void sendMessage(String userID, String linkToEvent, String dateTimeEvent) {
        JDA jda = DiscordBot.getJda();
        User user = jda.getUserById(userID);
        assert user != null;
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.ORANGE);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle(chooseTitle());
            builder.setDescription(chooseDescription());
            builder.addField("Szczegóły eventu", linkToEvent + "\n:date: " + dateTimeEvent, false);
            builder.setFooter("Więcej informacji i ustawień powiadomień pod komendą !help reminder");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private String chooseTitle() {
        String TITLE_ONE_HOUR = "**PRZYPOMNIENIE:** 60 minut do wydarzenia!";
        String TITLE_ONE_DAY = "**PRZYPOMNIENIE:** 1 dzień do wydarzenia!";
        return switch (typeOfReminder) {
            case ONE_HOUR -> TITLE_ONE_HOUR;
            case ONE_DAY -> TITLE_ONE_DAY;
        };
    }

    private String chooseDescription() {
        String DESCRIPTION_ONE_HOUR = "Wkrótce rozpocznie się wydarzenie na które się zapisałeś.";
        String DESCRIPTION_ONE_DAY = "Jutro wydarzenie na które się zapisałeś.";
        return switch (typeOfReminder) {
            case ONE_HOUR -> DESCRIPTION_ONE_HOUR;
            case ONE_DAY -> DESCRIPTION_ONE_DAY;
        };
    }
}

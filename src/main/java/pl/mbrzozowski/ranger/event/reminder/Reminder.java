package pl.mbrzozowski.ranger.event.reminder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mbrzozowski.ranger.Repository;
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

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String eventID;
    private final TypeOfReminder typeOfReminder;
    private final EventService eventService;


    /**
     * @param eventID - ID eventu, id wiadmości w której jest lista z zapisami.
     */
    public Reminder(String eventID, TypeOfReminder type, EventService eventService) {
        this.eventID = eventID;
        this.typeOfReminder = type;
        this.eventService = eventService;
    }

    @Override
    public void run() {
//        EventService event = Repository.getEvent();
        Optional<pl.mbrzozowski.ranger.event.Event> eventOptional = eventService.findEventByMsgId(eventID);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            UsersReminderOFF reminderOFF = new UsersReminderOFF();
            List<Player> mainList = eventService.getMainList(event);
            List<Player> reserveList = eventService.getReserveList(event);
            RangerLogger.info("Zapisanych na glównej liście: [" + mainList.size() + "], Rezerwa: [" +
                    reserveList.size() + "] - Wysyłam przypomnienia.", eventID);
            String linkToEvent = "[" + event.getName() + "](https://discord.com/channels/" +
                    CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" + event.getChannelId() + "/" + eventID + ")";
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
            String dateTimeEvent = event.getDate().format(dateFormat);
            for (Player value : mainList) {
                String userID = value.getUserId();
                if (!reminderOFF.userHasOff(userID)) {
                    sendMessage(userID, linkToEvent, dateTimeEvent);
                }
            }
            for (Player player : reserveList) {
                String userID = player.getUserId();
                if (!reminderOFF.userHasOff(userID)) {
                    sendMessage(userID, linkToEvent, dateTimeEvent);
                }
            }
        }
    }

    private void sendMessage(String userID, String linkToEvent, String dateTimeEvent) {
        JDA jda = Repository.getJda();
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

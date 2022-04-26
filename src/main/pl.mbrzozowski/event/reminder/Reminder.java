package event.reminder;

import embed.EmbedSettings;
import event.Event;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import model.MemberOfServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;
import java.util.List;
import java.util.TimerTask;

public class Reminder extends TimerTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String eventID;
    private TypeOfReminder typeOfReminder;
    private final String TITLE_ONE_HOUR = "**PRZYPOMNIENIE:** 60 minut do wydarzenia!";
    private final String TITLE_ONE_DAY = "**PRZYPOMNIENIE:** 1 dzień do wydarzenia!";
    private final String DESCRIPTION_ONE_HOUR = "Wkrótce rozpocznie się wydarzenie na które się zapisałeś.";
    private final String DESCRIPTION_ONE_DAY = "Jutro wydarzenie na które się zapisałeś.";


    /**
     * @param eventID - ID eventu, id wiadmości w której jest lista z zapisami.
     */
    public Reminder(String eventID, TypeOfReminder type) {
        this.eventID = eventID;
        this.typeOfReminder = type;
    }

    @Override
    public void run() {
        Event event = Repository.getEvent();
        int indexOfEvent = event.getIndexActiveEvent(eventID);
        if (indexOfEvent >= 0) {
            UsersReminderOFF reminderOFF = new UsersReminderOFF();
            List<MemberOfServer> mainList = event.getMainList(indexOfEvent);
            List<MemberOfServer> reserveList = event.getReserveList(indexOfEvent);
            RangerLogger.info("Zapisanych na glównej liście: [" + mainList.size() + "], Rezerwa: [" + reserveList.size() + "] - Wysyłam przypomnienia.", eventID);
            String linkToEvent = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" + CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" + event.getChannelID(eventID) + "/" + eventID + ")";
            String dateTimeEvent = event.getDateAndTimeFromEmbed(eventID);
            for (int i = 0; i < mainList.size(); i++) {
                String userID = mainList.get(i).getUserID();
                if (!reminderOFF.userHasOff(userID)) {
                    sendMessage(userID, linkToEvent, dateTimeEvent);
                }
            }
            for (int i = 0; i < reserveList.size(); i++) {
                String userID = reserveList.get(i).getUserID();
                if (!reminderOFF.userHasOff(userID)) {
                    sendMessage(userID, linkToEvent, dateTimeEvent);
                }
            }
        }
    }

    private void sendMessage(String userID, String linkToEvent, String dateTimeEvent) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
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

    private String chooseTitle(){
        switch (typeOfReminder){
            case ONE_HOUR:
                return TITLE_ONE_HOUR;
            case ONE_DAY:
                return TITLE_ONE_DAY;
            default:
                return "Błąd remindera";
        }
    }

    private String chooseDescription(){
        switch (typeOfReminder){
            case ONE_HOUR:
                return DESCRIPTION_ONE_HOUR;
            case ONE_DAY:
                return DESCRIPTION_ONE_DAY;
            default:
                return "Błąd remindera";
        }
    }
}

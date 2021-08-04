package event.reminder;

import embed.EmbedSettings;
import event.Event;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import model.MemberMy;
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
    private JDA jda = Repository.getJda();
    private Event event = Repository.getEvent();

    /**
     * @param eventID - ID eventu, id wiadmości w której jest lista z zapisami.
     */
    public Reminder(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public void run() {
        int indexOfEvent = event.getIndexActiveEvent(eventID);
        UsersReminderOFF reminderOFF = new UsersReminderOFF();
        if (indexOfEvent >= 0) {
            List<MemberMy> mainList = event.getMainList(indexOfEvent);
            List<MemberMy> reserveList = event.getReserveList(indexOfEvent);
            RangerLogger.info("Zapisanych na glównej liście: [" + mainList.size() + "], Rezerwa: [" + reserveList.size() + "] - Wysyłam przypomnienia.",eventID);
            for (int i = 0; i < mainList.size(); i++) {
                String userID = mainList.get(i).getUserID();
                if (!reminderOFF.userHasOff(userID)) {
                    sendMessage(userID);
                }
            }
            for (int i = 0; i < reserveList.size(); i++) {
                String userID = reserveList.get(i).getUserID();
                if (!reminderOFF.userHasOff(userID)) {
                    sendMessage(userID);
                }
            }
        }
    }

    private void sendMessage(String userID) {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            String link = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" + CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" + event.getChannelID(eventID) + "/" + eventID + ")";
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.ORANGE);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle("**PRZYPOMNIENIE:** 60 minut do wydarzenia!");
            builder.setDescription("Dostajesz to powiadomienie ponieważ znajdujesz się na liście eventu, który wkrótce się rozpocznie.");
            builder.addField("Szczegóły eventu", link + "\n:date: " + event.getDateAndTimeFromEmbed(eventID), false);
            builder.setFooter("Więcej informacji i ustawień powiadomień pod komendą !help reminder");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }
}

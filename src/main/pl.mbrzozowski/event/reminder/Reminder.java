package event.reminder;

import embed.EmbedSettings;
import event.Event;
import model.MemberMy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;
import java.util.List;
import java.util.TimerTask;

public class Reminder extends TimerTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String eventID;
    private JDA jda = RangerBot.getJda();
    private Event event = RangerBot.getEvents();

    /**
     * @param eventID - ID eventu, id wiadmości w której jest lista z zapisami.
     */
    public Reminder(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public void run() {
        //TODO pobrac sobie wszystkich uzytkownikow z bazy danych ktorzy nie chca dostawac powiadomien.
        int indexOfEvent = event.isActiveMatch(eventID);
        if (indexOfEvent>=0){
            List<MemberMy> mainList = event.getMainList(indexOfEvent);
            List<MemberMy> reserveList = event.getReserveList(indexOfEvent);
            for (int i = 0; i < mainList.size(); i++) {
                sendMessage(mainList.get(i).getUserID());
            }
            for (int i = 0; i < reserveList.size(); i++) {
                sendMessage(reserveList.get(i).getUserID());
            }
        }
    }

    private void sendMessage(String userID) {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setThumbnail("https://cdn.icon-icons.com/icons2/1993/PNG/512/clock_hour_minute_second_time_timer_watch_icon_123193.png");
            builder.setTitle("Halo Halo!!!");
            builder.setDescription("Pozostało 15 minut do rozpoczęcia wydarzenia na które się zapisałeś!");
            builder.setFooter("Więcej informacji i ustawień powiadomień pod komendą !helpReminder");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }
}

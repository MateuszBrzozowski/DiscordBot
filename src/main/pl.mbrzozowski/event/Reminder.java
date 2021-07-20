package event;

import embed.EmbedSettings;
import model.MemberMy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;

import java.awt.*;
import java.util.List;
import java.util.TimerTask;

public class Reminder extends TimerTask {

    private String eventID;
    JDA jda = RangerBot.getJda();

    /**
     * @param eventID - ID eventu, id wiadmości w której jest lista z zapisami.
     */
    public Reminder(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public void run() {
        //TODO pobrac sobie wszystkich uzytkownikow z bazy danych ktorzy nie chca dostawac powiadomien.
        Event event = RangerBot.getEvents();
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
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle("Halo halo - 15 minut!");
            builder.setDescription("Pozostało 15 minut do rozpoczęcia wydarzenia na które się zapisałeś!");
            builder.setFooter("Jeżeli nie chcesz otrzymywać takich powiadomień wpisz **!reminderEventUnSub**\n" +
                    "Możesz ponownie włączyć powiadomienia używając komendy **!reminderEventSub**");
        });
    }
}

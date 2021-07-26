package helpers;

import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;
import ranger.Repository;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RangerLogger {

    private final static String LOG_CHANNEL_ID = "860096729457098762";

    /**
     * @param msg         Wiadomość wypisana na kanale do logowania
     * @param channelName Nazwa kanału (np. eventu) na którym dane zdarzenie się wydarzyło.
     */
    public void info(String msg, String channelName) {
        msg = getCurrentDateAndTime() + " [" + channelName + "] - " + msg;
        Send(msg);
    }

    /**
     * @param msg Wiadomość wypisana na kanale do logowania
     */
    public void info(String msg) {
        msg = getCurrentDateAndTime() + " [RANGER-BOT] - " + msg;
        Send(msg);
    }

    private void Send(String msg) {
        JDA jda = Repository.getJda();
        jda.getTextChannelById(LOG_CHANNEL_ID).sendMessage(msg).queue();
    }

    private String getCurrentDateAndTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MM-yyyy HH:mm:ss.SSS");
        Date date = new Date();
        return dateFormat.format(date);
    }
}

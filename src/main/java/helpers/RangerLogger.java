package helpers;

import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RangerLogger {

    private final static String LOG_CHANNEL_ID = "860096729457098762";

    public void info(String msg, String channelName){
        msg = getCurrentDateAndTime() + " [" + channelName + "] - " + msg;
        Send(msg);
    }

    public void info(String msg){
        msg = getCurrentDateAndTime() + " [RANGER-BOT] - " + msg;
        Send(msg);
    }

    private void Send(String msg){
        JDA jda = RangerBot.getJda();
        jda.getTextChannelById(LOG_CHANNEL_ID).sendMessage(msg).queue();
    }

    private String getCurrentDateAndTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MM-yyyy HH:mm:ss.SSS");
        Date date = new Date();
        return dateFormat.format(date);
    }
}

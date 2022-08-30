package ranger.helpers;

import net.dv8tion.jda.api.JDA;
import ranger.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RangerLogger {

    private final static String LOG_CHANNEL_ID = "860096729457098762";

    /**
     * @param msg         Wiadomość wypisana na kanale do logowania
     * @param channelName Nazwa kanału (np. eventu) na którym dane zdarzenie się wydarzyło.
     */
    public static void info(String msg, String channelName) {
        msg = getCurrentDateAndTime() + " [" + channelName + "] - " + msg;
        Send(msg);
    }

    /**
     * @param msg Wiadomość wypisana na kanale do logowania
     */
    public static void info(String msg) {
        msg = getCurrentDateAndTime() + " [RANGER-BOT] - " + msg;
        Send(msg);
    }

    private static void Send(String msg) {
        JDA jda = Repository.getJda();
        jda.getTextChannelById(LOG_CHANNEL_ID).sendMessage(msg).queue();
    }

    private static String getCurrentDateAndTime() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE, dd-MM-yyyy HH:mm:ss.SSS");
        LocalDateTime date = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        return dateFormat.format(date);
    }
}

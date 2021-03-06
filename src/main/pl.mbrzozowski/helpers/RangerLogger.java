package helpers;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class RangerLogger {

    private final static String LOG_CHANNEL_ID = "860096729457098762";
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

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

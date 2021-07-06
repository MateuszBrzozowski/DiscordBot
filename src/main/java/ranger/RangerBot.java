package ranger;

import events.ButtonClickListener;
import events.ChannelUpdate;
import events.MessageUpdate;
import events.WriteListener;
import helpers.RangerLogger;
import model.Recruits;
import model.Event;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class RangerBot {
    private static Recruits recruits;
    private static Event matches;
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private static JDA jda;
    private static RangerLogger rangerLogger = new RangerLogger();

    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault("ODYxOTA1OTg1ODE5Mzc3NjY0.YOQmgA.ovdk1tinyHvCsfvAiLyDfPUyZ6k").build();
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.addEventListener(new WriteListener());
        jda.addEventListener(new ButtonClickListener());
        jda.addEventListener(new ChannelUpdate());
        jda.addEventListener(new MessageUpdate());
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initialize(jda);



//        rangerLogger.Info("Ranger-Bot uruchomiony poprawnie.");
        logger.info("Bot uruchomiony.");
    }

    private static void initialize(JDA jda) {
        recruits = new Recruits();
        recruits.initialize(jda);
        matches = new Event();
        matches.initialize(jda);
    }

    public static Recruits getRecruits() {
        return recruits;
    }

    public static Event getMatches() {
        return matches;
    }

    public static JDA getJda() {
        return jda;
    }
}

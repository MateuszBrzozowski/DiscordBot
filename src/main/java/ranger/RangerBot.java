package ranger;

import events.ButtonClickListener;
import events.ChannelUpdate;
import events.WriteListener;
import model.Recruits;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class RangerBot {
    private static Recruits recruits;
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault("ODU0MTc1NjAwODk3NDI1NDc4.YMgHBQ.nt5FXodl199DFKt2_0WOqG5tN5A").build();
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.addEventListener(new WriteListener());
        jda.addEventListener(new ButtonClickListener());
        jda.addEventListener(new ChannelUpdate());
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initialize(jda);
        logger.info("Bot uruchomiony.");
    }

    private static void initialize(JDA jda) {
        recruits = new Recruits();
        recruits.initialize(jda);
    }

    public static Recruits getRecruits() {
        return recruits;
    }
}

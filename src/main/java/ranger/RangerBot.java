package ranger;

import events.ChannelDelete;
import model.Recruits;
import events.ButtonClickListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import events.WriteListener;

import javax.security.auth.login.LoginException;

public class RangerBot {
    private static Recruits recruits;


    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault("ODU0MTc1NjAwODk3NDI1NDc4.YMgHBQ.Vo1-Uti-lhML8yOjKAHd-zrw2kg").build();
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.addEventListener(new WriteListener());
        jda.addEventListener(new ButtonClickListener());
        jda.addEventListener(new ChannelDelete());
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initialize(jda);

    }

    private static void initialize(JDA jda) {
        recruits = new Recruits();
        recruits.initialize(jda);
    }

    public static Recruits getRecruits() {
        return recruits;
    }
}

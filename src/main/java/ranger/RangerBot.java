package ranger;

import events.*;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import helpers.RoleID;
import model.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RangerBot {
    private static Recruits recruits;
    private static Event matches;
    private static EventsGeneratorModel eventsGeneratorModel;
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private static JDA jda;
    private static RangerLogger rangerLogger = new RangerLogger();



    public static void main(String[] args) throws LoginException {
        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        jda = JDABuilder.create("ODYxOTA1OTg1ODE5Mzc3NjY0.YOQmgA.ovdk1tinyHvCsfvAiLyDfPUyZ6k", intents)
                .addEventListeners(new WriteListener())
                .addEventListeners(new ButtonClickListener())
                .addEventListeners(new ChannelUpdate())
                .addEventListeners(new MessageUpdate())
                .addEventListeners(new Listener())
                .build();
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
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
        matches = new Event();
        matches.initialize(jda);
        eventsGeneratorModel = new EventsGeneratorModel();
//        discordServer = new DiscordServer();
//        discordServer.initialize(jda);

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

    public static EventsGeneratorModel getEventsGeneratorModel() {
        return eventsGeneratorModel;
    }
}

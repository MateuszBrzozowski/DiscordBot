package ranger;

import bot.event.*;
import event.Event;
import event.EventsGeneratorModel;
import helpers.RangerLogger;
import model.DiceGames;
import recrut.Recruits;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;

public class RangerBot {

    private static final String BOT_TOKEN = "";
    private static Recruits recruits;
    private static Event events;
    private static DiceGames diceGames;
    private static EventsGeneratorModel eventsGeneratorModel;
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private static JDA jda;
    private static RangerLogger rangerLogger = new RangerLogger();


    public static void main(String[] args) throws LoginException {

        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        jda = JDABuilder.create(BOT_TOKEN, intents)
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
        events = new Event();
        events.initialize(jda);
        eventsGeneratorModel = new EventsGeneratorModel();
        diceGames = new DiceGames();

    }

    public static Recruits getRecruits() {
        return recruits;
    }

    public static Event getEvents() {
        return events;
    }

    public static JDA getJda() {
        return jda;
    }

    public static EventsGeneratorModel getEventsGeneratorModel() {
        return eventsGeneratorModel;
    }

    public static DiceGames getDiceGames() {
        return diceGames;
    }
}

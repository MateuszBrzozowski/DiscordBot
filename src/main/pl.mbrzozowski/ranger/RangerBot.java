package ranger;

import bot.event.*;
import counter.Counter;
import event.Event;
import helpers.RangerLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import questionnaire.Questionnaires;
import recrut.Recruits;
import server.service.ServerService;
import server.whitelist.Whitelist;
import stats.ServerStats;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;

public class RangerBot {

    private static final String BOT_TOKEN = "";
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    public static void main(String[] args) throws LoginException {

        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        JDA jda = JDABuilder.create(BOT_TOKEN, intents)
                .addEventListeners(new WriteListener())
                .addEventListeners(new ButtonClickListener())
                .addEventListeners(new ChannelUpdate())
                .addEventListeners(new MessageUpdate())
                .addEventListeners(new Listener())
                .addEventListeners(new ModalListener())
                .addEventListeners(new SelecetMenuListener())
                .build();
        Repository.setJDA(jda);
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initialize();

        RangerLogger.info("Bot uruchomiony.");
    }

    private static void initialize() {
        Thread whitelistThread = new Thread(() -> {
            Whitelist whitelist = new Whitelist();
            whitelist.whitelistUpdate();
        });
        whitelistThread.start();
        Recruits recruits = Repository.getRecruits();
        recruits.initialize();
        Event events = Repository.getEvent();
        events.initialize();
        Questionnaires questionnaires = Repository.getQuestionnaires();
        questionnaires.initialize();
        Counter counter = Repository.getCounter();
        counter.initialize();
        ServerService serverService = Repository.getServerService();
        serverService.initialize();
        ServerStats serverStats = Repository.getServerStats();
        serverStats.initialize();
    }
}

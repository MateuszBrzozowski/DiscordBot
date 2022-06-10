package ranger;

import bot.event.*;
import helpers.Constants;
import helpers.RangerLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.whitelist.Whitelist;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RangerBot {

    private static final String BOT_TOKEN = Constants.TOKEN_RANGER_TESTER;
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
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(() -> Repository.getRecruits().initialize());
        executorService.submit(() -> Repository.getEvent().initialize());
        executorService.submit(() -> Repository.getQuestionnaires().initialize());
        executorService.submit(() -> Repository.getCounter().initialize());
        executorService.submit(() -> Repository.getServerService().initialize());
        executorService.submit(() -> Repository.getServerStats().initialize());
        executorService.submit(Whitelist::whitelistUpdate);
        executorService.shutdown();
    }
}

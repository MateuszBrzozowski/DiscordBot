package pl.mbrzozowski.ranger;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mbrzozowski.ranger.bot.events.*;
import pl.mbrzozowski.ranger.helpers.Constants;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class DiscordBot {

    private final WriteListener writeListener;
    private final ButtonClickListener buttonClickListener;
    private final ChannelUpdate channelUpdate;
    private final MessageUpdate messageUpdate;
    private final Listener listener;
    private final SelectMenuListener selectMenuListener;
    private static JDA jda;

    @Autowired
    public DiscordBot(WriteListener writeListener,
                      ButtonClickListener buttonClickListener,
                      ChannelUpdate channelUpdate,
                      MessageUpdate messageUpdate,
                      Listener listener,
                      SelectMenuListener selectMenuListener) throws LoginException {
        this.writeListener = writeListener;
        this.buttonClickListener = buttonClickListener;
        this.channelUpdate = channelUpdate;
        this.messageUpdate = messageUpdate;
        this.listener = listener;
        this.selectMenuListener = selectMenuListener;
        DiscordBotRun();
    }

    private void DiscordBotRun() throws LoginException {
        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        JDA jda = JDABuilder.create(Constants.TOKEN_RANGER_TESTER, intents)
                .addEventListeners(this.writeListener)
                .addEventListeners(this.buttonClickListener)
                .addEventListeners(this.channelUpdate)
                .addEventListeners(this.messageUpdate)
                .addEventListeners(this.listener)
                .addEventListeners(new ModalListener())
                .addEventListeners(this.selectMenuListener)
                .build();
//        Repository.setJDA(jda);
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        DiscordBot.jda = jda;
    }

    public static JDA getJda() {
        return jda;
    }

    //    @Bean
//    @Lazy
//    private @NotNull JDA getJDA() throws LoginException {
//        Collection<GatewayIntent> intents = new ArrayList<>();
//        intents.add(GatewayIntent.GUILD_MEMBERS);
//        intents.add(GatewayIntent.GUILD_MESSAGES);
//        intents.add(GatewayIntent.DIRECT_MESSAGES);
//        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
//        JDA jda = JDABuilder.create(Constants.TOKEN_RANGER_TESTER, intents)
//                .addEventListeners(this.writeListener)
//                .addEventListeners(this.buttonClickListener)
//                .addEventListeners(this.channelUpdate)
//                .addEventListeners(this.messageUpdate)
//                .addEventListeners(new Listener())
//                .addEventListeners(new ModalListener())
//                .addEventListeners(new SelecetMenuListener())
//                .build();
//        Repository.setJDA(jda);
//        jda.getPresence().setActivity(Activity.listening("Spotify"));
//        jda.getPresence().setStatus(OnlineStatus.ONLINE);
//        return jda;
//    }


}

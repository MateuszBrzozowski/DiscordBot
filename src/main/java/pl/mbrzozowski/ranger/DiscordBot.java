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
    private final SlashCommandListener slashCommandListener;
    private static JDA jda;

    @Autowired
    public DiscordBot(WriteListener writeListener,
                      ButtonClickListener buttonClickListener,
                      ChannelUpdate channelUpdate,
                      MessageUpdate messageUpdate,
                      Listener listener,
                      SelectMenuListener selectMenuListener,
                      SlashCommandListener slashCommandListener) throws LoginException {
        this.writeListener = writeListener;
        this.buttonClickListener = buttonClickListener;
        this.channelUpdate = channelUpdate;
        this.messageUpdate = messageUpdate;
        this.listener = listener;
        this.selectMenuListener = selectMenuListener;
        this.slashCommandListener = slashCommandListener;
        DiscordBotRun();
    }

    private void DiscordBotRun() throws LoginException {
        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        intents.add(GatewayIntent.MESSAGE_CONTENT);
        JDA jda = JDABuilder.create(Constants.TOKEN_RANGER_TESTER, intents)
                .addEventListeners(this.writeListener)
                .addEventListeners(this.buttonClickListener)
                .addEventListeners(this.channelUpdate)
                .addEventListeners(this.messageUpdate)
                .addEventListeners(this.listener)
                .addEventListeners(new ModalListener())
                .addEventListeners(this.selectMenuListener)
                .addEventListeners(this.slashCommandListener)
                .build();
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        DiscordBot.jda = jda;
    }

    public static JDA getJda() {
        return jda;
    }
}

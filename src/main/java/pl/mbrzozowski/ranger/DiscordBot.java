package pl.mbrzozowski.ranger;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.mbrzozowski.ranger.bot.events.*;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class DiscordBot {

    private final WriteListener writeListener;
    private final ButtonClickListener buttonClickListener;
    private final ChannelUpdate channelUpdate;
    private final MessageUpdate messageUpdate;
    private final GuildMemberListener guildMemberListener;
    private final SelectMenuListener selectMenuListener;
    private final SlashCommandListener slashCommandListener;
    private final ModalListener modalListener;
    private static JDA jda;
    private final String token;

    @Autowired
    public DiscordBot(WriteListener writeListener,
                      ButtonClickListener buttonClickListener,
                      ChannelUpdate channelUpdate,
                      MessageUpdate messageUpdate,
                      GuildMemberListener guildMemberListener,
                      SelectMenuListener selectMenuListener,
                      SlashCommandListener slashCommandListener,
                      ModalListener modalListener,
                      @Value("${discord.token}") String token) throws LoginException {
        this.writeListener = writeListener;
        this.buttonClickListener = buttonClickListener;
        this.channelUpdate = channelUpdate;
        this.messageUpdate = messageUpdate;
        this.guildMemberListener = guildMemberListener;
        this.selectMenuListener = selectMenuListener;
        this.slashCommandListener = slashCommandListener;
        this.modalListener = modalListener;
        this.token = token;
        DiscordBotRun();
    }

    private void DiscordBotRun() throws LoginException {
        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        intents.add(GatewayIntent.MESSAGE_CONTENT);
        JDA jda = JDABuilder.create(token, intents)
                .addEventListeners(this.writeListener)
                .addEventListeners(this.buttonClickListener)
                .addEventListeners(this.channelUpdate)
                .addEventListeners(this.messageUpdate)
                .addEventListeners(this.guildMemberListener)
                .addEventListeners(this.modalListener)
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

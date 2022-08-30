package ranger;

import bot.event.*;
import helpers.Constants;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;

@Component
@Getter
public class Config {
    private final JDA jda;

    public Config() throws LoginException {
        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.DIRECT_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        jda = JDABuilder.create(Constants.TOKEN_RANGER_TESTER, intents)
                .addEventListeners(new WriteListener())
                .addEventListeners(new ButtonClickListener())
                .addEventListeners(new ChannelUpdate())
                .addEventListeners(new MessageUpdate())
                .addEventListeners(new Listener())
                .addEventListeners(new ModalListener())
                .addEventListeners(new SelecetMenuListener())
                .build();
//        Repository.setJDA(jda);
        jda.getPresence().setActivity(Activity.listening("Spotify"));
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
    }


}

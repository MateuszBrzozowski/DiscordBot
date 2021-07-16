package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EmbedYouHaveActiveEventGenerator {

    public EmbedYouHaveActiveEventGenerator(@NotNull GuildMessageReceivedEvent event) {
        String userID = event.getMessage().getAuthor().getId();
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("MIAŁEŚ AKTYWNY GENERATOR");
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    public EmbedYouHaveActiveEventGenerator(PrivateMessageReceivedEvent event) {
        String userID = event.getMessage().getAuthor().getId();
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("MIAŁEŚ AKTYWNY GENERATOR");
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}

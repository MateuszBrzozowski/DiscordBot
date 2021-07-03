package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EmbedNoWriteOnLoggerChannel {

    public EmbedNoWriteOnLoggerChannel(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()){
            event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
                user.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.RED);
                    builder.setTitle("Zachowajmy porządek!");
                    builder.setDescription("Panie administratorze! Zachowajmy czystość na kanale do loggowania. Proszę nie wtrącać się w moje wypociny.");
                    builder.setFooter("RangerBot created by Brzozaaa © 2021");
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    privateChannel.sendMessage(builder.build()).queue();
                });
            });
        }
    }
}

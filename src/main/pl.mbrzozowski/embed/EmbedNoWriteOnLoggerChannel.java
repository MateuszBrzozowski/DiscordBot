package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;

import java.awt.*;

public class EmbedNoWriteOnLoggerChannel {

    public EmbedNoWriteOnLoggerChannel(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            event.getMessage().delete().submit();
            JDA jda = Repository.getJda();
            jda.getUserById(event.getAuthor().getId()).openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("Zachowajmy porządek!");
                builder.setDescription("Panie administratorze! Zachowajmy czystość na kanale do loggowania. Proszę nie wtrącać się w moje wypociny.");
                builder.setFooter("RangerBot created by Brzozaaa © 2021");
                builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
                privateChannel.sendMessage(builder.build()).queue();
            });
        }
    }
}

package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

public class EmbedRemoveChannel {

    public EmbedRemoveChannel(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Kanał usunięty.");
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).queue();
    }
}

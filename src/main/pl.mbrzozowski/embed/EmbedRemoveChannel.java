package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class EmbedRemoveChannel {

    public EmbedRemoveChannel(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Kanał usunięty.");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessage(builder.build()).queue();
    }
}

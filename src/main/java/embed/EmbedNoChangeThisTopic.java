package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateTopicEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EmbedNoChangeThisTopic {

    public EmbedNoChangeThisTopic(@NotNull TextChannelUpdateTopicEvent event) {
        String oldTopic = event.getOldTopic();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("Wykryto zmianę tematu");
        builder.setDescription("Nie zmieniaj tematu tego kanału. Może to zaburzyć prawidłowe funkcjonowanie bota.");
        builder.addField("Poprzedni temat tego kanału",oldTopic,false);
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).queue();
    }
}

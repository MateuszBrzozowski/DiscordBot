package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EmbedNoChangeThisName {

    public EmbedNoChangeThisName(@NotNull TextChannelUpdateNameEvent event) {
        String oldTopic = event.getOldName();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("Wykryto zmianę nazwy kanału");
        builder.setDescription("Nie zmieniaj nazwy tego kanału. Może to zaburzyć prawidłowe funkcjonowanie bota.");
        builder.addField("Poprzednia nazwa tego kanału", oldTopic, false);
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).queue();
    }
}

package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

public class EmbedNegative {

    public EmbedNegative(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("WYNIK REKRUTACJI - NEGATYWNY");
        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).queue();
    }
}

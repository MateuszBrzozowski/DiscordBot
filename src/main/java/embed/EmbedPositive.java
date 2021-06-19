package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.entities.UserById;

import java.awt.*;

public class EmbedPositive {

    public EmbedPositive(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        EmbedBuilder builder = new EmbedBuilder();
        String name = event.getAuthor().getName();
        event.getChannel().sendMessage("Gratulacje {@user}").queue();
        builder.setColor(Color.GREEN);
        builder.setTitle("WYNIK REKRUTACJI - POZYTYWNY");
        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).queue();
    }
}

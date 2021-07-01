package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;

import java.awt.*;

public class EmbedCantSignOut {

    public EmbedCantSignOut(String userID) {
        JDA jda = RangerBot.getJda();
        jda.retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.red);
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setTitle("Nie możesz wypisać się z tego meczu.");
                builder.setDescription("Nie możesz wypisać się z meczu na który się nie zapisałeś!");
                builder.addField("","Jeżeli jednak jesteś na liście a nadl otrzymujesz tą wiadomość. Napisz do administracji.", false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}

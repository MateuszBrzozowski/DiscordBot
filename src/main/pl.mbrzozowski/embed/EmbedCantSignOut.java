package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;
import ranger.Repository;

import java.awt.*;

public class EmbedCantSignOut {

    public EmbedCantSignOut(String userID) {
        JDA jda = Repository.getJda();
        jda.retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.red);
                builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
                builder.setTitle("Nie możesz wypisać się z tego meczu.");
                builder.setDescription("Nie możesz wypisać się z meczu na który się nie zapisałeś!");
                builder.addField("", "Jeżeli jednak jesteś na liście a nadal otrzymujesz tą wiadomość. Napisz do administracji.", false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}

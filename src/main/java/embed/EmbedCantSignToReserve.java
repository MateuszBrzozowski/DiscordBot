package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.awt.*;

public class EmbedCantSignToReserve {
    public EmbedCantSignToReserve(ButtonClickEvent event, String userID) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.red);
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setTitle("Jesteś już na liście rezerwowej");
                builder.setDescription("Jesteś już na rezerwowej liście w meczu na który próbowałeś się zapisać.");
                builder.addField("","Jeżeli nie widzisz siebie na liście, nie możesz się zapisać bo otrzymujesz tą wiadomość. Napisz do administracji.", false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}

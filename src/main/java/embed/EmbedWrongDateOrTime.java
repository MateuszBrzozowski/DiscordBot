package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;

import java.awt.*;

public class EmbedWrongDateOrTime {

    public EmbedWrongDateOrTime(String userID) {
        JDA jda = RangerBot.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("Podałeś nieprawidłowe dane.");
            builder.setDescription("Format daty: dd.MM.yyyy\n" +
                    "Format czasu: hh:mm");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }
}

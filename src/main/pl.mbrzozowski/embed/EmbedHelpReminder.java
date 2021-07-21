package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import ranger.RangerBot;

import java.awt.*;

public class EmbedHelpReminder {

    public EmbedHelpReminder(String userID) {
        JDA jda = RangerBot.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Ranger Bot - POMOC - REMINDER");
            builder.setFooter("RangerBot created by Brzozaaa © 2021");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setColor(Color.YELLOW);
            builder.setDescription("15 minut przed każdym eventem rozsyłane są przypomnienia do każdego zapisanego użytkownika. Możesz je dla siebie wyłączyć i włączyć przy pomocy komend.\n\n" +
                    "**!reminderOff** - Wyłącza powiadomienia\n" +
                    "**!reminderOn** - Włącza powiadomienia");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }
}

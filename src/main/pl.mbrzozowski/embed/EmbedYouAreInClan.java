package embed;

import helpers.RangerLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import ranger.Repository;

import java.awt.*;

public class EmbedYouAreInClan {
    private RangerLogger rangerLogger = new RangerLogger();

    public EmbedYouAreInClan(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setDescription("Masz przypisaną rolę innego klanu na naszym discordzie.");
            builder.addBlankField(false);
            builder.addField("- Nie należę do żadnego klanu", "Proszę znajdź użytkownika z rolą Rada klanu na naszym discordzie i " +
                    "napisz do nas.", false);
            privateChannel.sendMessage(builder.build()).queue();
            rangerLogger.info("Użytkonik [" + jda.getUserById(userID).getName() + "] chciał złożyć podanie. Ma przypisaną rolę innego klanu.");
        });
    }
}

package embed;

import helpers.RangerLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import ranger.Repository;

import java.awt.*;

public class EmbedYouAreClanMember {
    private RangerLogger rangerLogger = new RangerLogger();

    public EmbedYouAreClanMember(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setDescription("Jesteś już w naszym klanie dzbanie!");
            privateChannel.sendMessage(builder.build()).queue();
            rangerLogger.info("Użytkonik [" + jda.getUserById(userID).getName() + "] chciał złożyć podanie. Jest już w naszym klanie.");
        });
    }
}

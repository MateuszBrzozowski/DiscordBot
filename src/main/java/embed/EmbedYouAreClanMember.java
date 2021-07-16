package embed;

import helpers.RangerLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.awt.*;

public class EmbedYouAreClanMember {
    private RangerLogger rangerLogger = new RangerLogger();

    public EmbedYouAreClanMember(ButtonClickEvent event) {
        event.getJDA().retrieveUserById(event.getUser().getId()).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
                builder.setColor(Color.red);
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setDescription("Jesteś już w naszym klanie dzbanie!");
                privateChannel.sendMessage(builder.build()).queue();
                rangerLogger.info("Użytkonik [" + user.getName() + "] chciał złożyć podanie. Jest już w naszym klanie.");
            });
        });
    }
}

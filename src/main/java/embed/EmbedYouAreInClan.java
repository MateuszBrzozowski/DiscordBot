package embed;

import helpers.RangerLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.awt.*;

public class EmbedYouAreInClan {
    private RangerLogger rangerLogger = new RangerLogger();

    public EmbedYouAreInClan(ButtonClickEvent event) {
        event.getJDA().retrieveUserById(event.getUser().getId()).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
                builder.setColor(Color.red);
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setDescription("Masz przypisaną rolę innego klanu na naszym discordzie.");
                builder.addBlankField(false);
                builder.addField("- Nie należę do żadnego klanu", "Proszę znajdź użytkownika z rolą Rada klanu na naszym discordzie i " +
                        "napisz do nas.", false);
                privateChannel.sendMessage(builder.build()).queue();
                rangerLogger.info("Użytkonik ["+user.getName()+"] chciał złożyć podanie. Ma przypisaną rolę innego klanu.");
            });
        });
    }
}

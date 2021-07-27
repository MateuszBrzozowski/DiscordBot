package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import ranger.Repository;

import java.awt.*;

public class EmbedInfoEditEventChannel {

    public EmbedInfoEditEventChannel(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setFooter("RangerBot created by Brzozaaa © 2021");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle("Tworzenie eventu na kanale - POMOC");
            builder.addField("", "Jeżeli potrzebujesz dodaj swój opis na kanale eventu, a następnie stwórz listę " +
                    "przy pomocy poniższych komend", false);
            builder.addField("Wszystkie komendy wpisuj na kanale eventu", "**!name <nazwa>** - zmienia nazwę kanału\n" +
                    "**!generatorHere** - uruchamia generator tworzenia eventów\n" +
                    "lub stwórz listę bez generatora używając komendy **!zapisyhere** <- więcej informacji jak tworzyć " +
                    "listę w pomocy bota pod komendą **!help**", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }
}

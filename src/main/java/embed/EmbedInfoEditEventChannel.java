package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;

public class EmbedInfoEditEventChannel {

    public EmbedInfoEditEventChannel(Guild guild, String userID) {
        guild.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.setFooter("RangerBot created by Brzozaaa © 2021");
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setTitle("Tworzenie eventu na kanale - POMOC");
                builder.addField("","Jeżeli potrzebujesz dodaj swój opis na kanale eventu a następnie stwórz listę " +
                        "przy pomocy poniższych komend",false);
                builder.addField("Wszystkie komendy wpisuj na kanale eventu","**!name <nazwa>** - zmienia nazwę kanału\n" +
                        "**!generatorHere** - uruchamia generator który doda listę na kanale\n" +
                        "lub stwórz listę bez generatora używając komendy !zapisyhere <- więcej informacji jak tworzyć " +
                        "listę przy pomocy komend znajdują się w pomocy bota pod komendą **!help**",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}

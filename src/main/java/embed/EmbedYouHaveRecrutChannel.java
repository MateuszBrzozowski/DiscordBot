package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.awt.*;

public class EmbedYouHaveRecrutChannel {
    public EmbedYouHaveRecrutChannel(ButtonClickEvent event, String userID) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("NIE MOŻESZ ZŁOŻYĆ WIĘCEJ NIŻ JEDNO PODANIE!");
                builder.setColor(Color.red);
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setDescription("Zlożyłeś już podanie do naszego klanu i\n" +
                        "jesteś w trakcie rekrutacji.\n");
                builder.addField("Jeżeli masz pytania w związku z Twoją rekrutacją", "", false);
                builder.addField("1. Spradź kanały", "Znajdź kanał przypisany do twojej rekrutacji i napisz do nas.", false);
                builder.addField("2.Nie widze kanału.", "Jeżeli nie widzisz kanału przypisanego do twojej rekrutacji skontaktuj się z Drill Instrutor. Znajdziesz ich po prawej stronie na liście użytkowników.", false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}

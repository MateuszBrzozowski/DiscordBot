package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class EmbedDice {

    public EmbedDice(@NotNull GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        EmbedBuilder builder = new EmbedBuilder();
        int liczba = losujLiczbę();
        builder.setTitle("Wylosowana liczba:");
        builder.addField("", String.valueOf(liczba),false);
        builder.setThumbnail("https://www.pikpng.com/pngl/m/57-572648_clipart-single-die-crapola-a-game-of-dice.png");
        event.getChannel().sendMessage(builder.build()).queue();
    }

    private int losujLiczbę() {
        Random random = new Random();
        int liczba = random.nextInt(6)+1;
        return liczba;
    }
}

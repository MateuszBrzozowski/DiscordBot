package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public class EmbedDice {

    public EmbedDice(@NotNull GuildMessageReceivedEvent event) {
        String userName = event.getMessage().getMember().getNickname();
        if (userName==null){
            userName = event.getMessage().getAuthor().getName();
        }
        EmbedBuilder builder = new EmbedBuilder();
        int liczba = losujLiczbę();
        builder.setColor(Color.WHITE);
        builder.setTitle("Wylosowana liczba:");
        builder.addField(String.valueOf(liczba), "",false);
        builder.setThumbnail("https://www.iconsdb.com/icons/download/white/dice-64.png");
        builder.setFooter(userName);
        event.getChannel().sendMessage(builder.build()).queue();
    }

    private int losujLiczbę() {
        Random random = new Random();
        int liczba = random.nextInt(6)+1;
        return liczba;
    }
}

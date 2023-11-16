package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.util.Random;

public class Dice {

    public static void start(@NotNull SlashCommandInteractionEvent event) {
        int number = drawNumber();
        showResult(event, number);
    }

    private static void showResult(@NotNull SlashCommandInteractionEvent event, int number) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail(EmbedSettings.THUMBNAIL_DICE);
        builder.setTitle("Wylosowana liczba:");
        builder.addField(String.valueOf(number), "", false);
        event.reply("<@" + event.getUser().getId() + ">").setEmbeds(builder.build()).queue();
    }

    private static int drawNumber() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }
}

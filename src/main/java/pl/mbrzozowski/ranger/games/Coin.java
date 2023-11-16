package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public class Coin {

    public static void start(@NotNull SlashCommandInteractionEvent event) {
        int number = drawNumber();
        String result = convertNumToString(number);
        showResult(event, result);
    }

    private static void showResult(@NotNull SlashCommandInteractionEvent event, String result) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setTitle("Wynik:");
        builder.addField(result, "", false);
        event.reply("<@" + event.getUser().getId() + ">").setEmbeds(builder.build()).queue();
    }

    @NotNull
    protected static String convertNumToString(int number) {
        if (number == 0) {
            return "Orzeł";
        } else if (number == 1) {
            return "Reszka";
        } else {
            throw new IllegalArgumentException("Number must be 0 or 1");
        }
    }

    private static int drawNumber() {
        Random random = new Random();
        return random.nextInt(2);
    }
}

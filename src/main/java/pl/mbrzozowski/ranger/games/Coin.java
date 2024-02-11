package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.util.Random;

public class Coin {

    public static void start(@NotNull SlashCommandInteractionEvent event) {
        int number = drawNumber();
        String result = convertNumToString(number);
        showResult(event, result, number);
    }

    private static void showResult(@NotNull SlashCommandInteractionEvent event, String result, int resultAsInt) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(EmbedSettings.THUMBNAIL_COIN);
        if (resultAsInt == 0) {
            builder.setColor(Color.GREEN);
        } else {
            builder.setColor(Color.RED);
        }
        builder.setDescription("# Wynik: " + result);
        event.reply(event.getUser().getAsMention()).setEmbeds(builder.build()).queue();
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

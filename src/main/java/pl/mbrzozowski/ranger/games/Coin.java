package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static pl.mbrzozowski.ranger.guild.SlashCommands.COIN;

public class Coin implements SlashCommandGame {

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(COIN.getName(), COIN.getDescription()));
    }

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        int number = drawNumber();
        String result = convertNumToString(number);
        showResult(event, result, number);
    }

    private void showResult(@NotNull SlashCommandInteractionEvent event, String result, int resultAsInt) {
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
    protected String convertNumToString(int number) {
        if (number == 0) {
            return "Orzeł";
        } else if (number == 1) {
            return "Reszka";
        } else {
            throw new IllegalArgumentException("Number must be 0 or 1");
        }
    }

    private int drawNumber() {
        Random random = new Random();
        return random.nextInt(2);
    }
}

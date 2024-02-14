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

import static pl.mbrzozowski.ranger.guild.SlashCommands.DICE;

public class Dice implements SlashCommandGame {

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        int number = drawNumber();
        showResult(event, number);
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(DICE.getName(), DICE.getDescription()));
    }

    private void showResult(@NotNull SlashCommandInteractionEvent event, int number) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail(EmbedSettings.THUMBNAIL_DICE);
        builder.setDescription("# Wynik: " + number);
        event.reply(event.getUser().getAsMention()).setEmbeds(builder.build()).queue();
    }

    private int drawNumber() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }
}

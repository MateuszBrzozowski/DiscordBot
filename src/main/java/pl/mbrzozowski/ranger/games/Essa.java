package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Math;
import pl.mbrzozowski.ranger.model.SlashCommand;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

import static pl.mbrzozowski.ranger.guild.SlashCommands.ESSA;

public class Essa implements SlashCommandGame, SlashCommand {

    private final HashSet<EssaPlayer> players = new HashSet<>();
    private static final Essa instance = new Essa();

    private Essa() {
    }

    public static Essa getInstance() {
        return instance;
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(ESSA.getName(), ESSA.getDescription()));
    }

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        EssaPlayer essaPlayer = new EssaPlayer(LocalDate.now(), event.getUser().getId(), 0);
        Optional<EssaPlayer> existPlayer = isExist(essaPlayer);
        if (existPlayer.isEmpty()) {
            int number = drawNumber();
            number = roundTo5(number);
            essaPlayer.setEssaValue(number);
            players.add(essaPlayer);
            showResult(event, number);
        } else {
            showResult(event, existPlayer.get().getEssaValue());
        }
    }

    @NotNull
    protected Optional<EssaPlayer> isExist(EssaPlayer essaPlayer) {
        players.removeIf(player -> player.getDate() != null && !player.getDate().equals(essaPlayer.getDate()));
        return players
                .stream()
                .filter(player -> StringUtils.isNotBlank(player.getUserId()) && player.getUserId().equals(essaPlayer.getUserId()))
                .findFirst();
    }

    private void showResult(@NotNull SlashCommandInteractionEvent event, int number) {
        event.reply("<@" + event.getUser().getId() + "> Twój dzisiejszy poziom essy wynosi " + number + "%").queue();
    }

    protected int roundTo5(int number) {
        if (number >= 0 && number <= 100) {
            return Math.roundTo5(number);
        } else {
            throw new IllegalArgumentException("Number must be in the range 0..100");
        }
    }

    private int drawNumber() {
        Random random = new Random();
        return random.nextInt(101);
    }
}

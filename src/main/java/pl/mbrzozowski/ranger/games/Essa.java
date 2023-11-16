package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mbrzozowski.ranger.helpers.Math;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Essa {

    private static final Set<EssaPlayer> players = new HashSet<>();

    public static void start(@NotNull SlashCommandInteractionEvent event) {
        EssaPlayer essaPlayer = new EssaPlayer(LocalDate.now(), event.getUser().getId(), 0);
        EssaPlayer existPlayer = isExist(essaPlayer);
        if (existPlayer == null) {
            int number = drawNumber();
            number = roundTo5(number);
            essaPlayer.setEssaValue(number);
            players.add(essaPlayer);
            showResult(event, number);
        } else {
            showResult(event,existPlayer.getEssaValue());
        }
    }

    @Nullable
    protected static EssaPlayer isExist(EssaPlayer essaPlayer) {
        if (players.contains(essaPlayer)) {
            for (EssaPlayer player : players) {
                if (player.equals(essaPlayer)) {
                    if (player.getDate().equals(essaPlayer.getDate())) {
                        return player;
                    } else {
                        players.remove(player);
                    }
                }
            }
        }
        return null;
    }

    private static void showResult(@NotNull SlashCommandInteractionEvent event, int number) {
        event.reply("<@" + event.getUser().getId() + "> Twój dzisiejszy poziom essy wynosi " + number + "%").queue();
    }

    protected static int roundTo5(int number) {
        if (number >= 0 && number <= 100) {
            return Math.roundTo5(number);
        } else {
            throw new IllegalArgumentException("Number must be in the range 0..100");
        }
    }

    private static int drawNumber() {
        Random random = new Random();
        return random.nextInt(101);
    }
}

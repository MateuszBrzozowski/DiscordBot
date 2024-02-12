package pl.mbrzozowski.ranger.games;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.guild.SlashCommands;
import pl.mbrzozowski.ranger.model.SlashCommand;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RandomTimeout implements SlashCommand, SlashCommandGame {


    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SlashCommands.RANDOM_TIMEOUT.getName(), SlashCommands.RANDOM_TIMEOUT.getDescription()));
    }

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        Random random = new Random();
        boolean isWin = random.nextBoolean();
        if (!isWin) {
            event.reply("Nie wygrałeś. Graj dalej!").setEphemeral(true).queue();
            return;
        }
        Guild guild = RangersGuild.getGuild();
        if (guild == null) {
            throw new NullPointerException("Guild null");
        }
        int time = random.nextInt(1430) + 10;
        guild.timeoutFor(event.getUser(), time, TimeUnit.MINUTES).queue();
        event.reply(event.getUser().getAsMention() + " " + getMessage(time)).setEphemeral(true).queue();
    }

    @NotNull
    private String getMessage(int time) {
        if (time <= 60) {
            return "Wygrał timeout na " + time + " minut";
        }
        int hour = time / 60;
        String hoursAsString;
        if (hour == 1) {
            hoursAsString = "godzinę";
        } else if (hour == 2 || hour == 3 || hour == 4 || hour == 22 || hour == 23 || hour == 24) {
            hoursAsString = "godziny";
        } else {
            hoursAsString = "godzin";
        }
        return "Wygrał timeout na " + hour + " " + hoursAsString + " i " + time % 60 + " minut";
    }
}

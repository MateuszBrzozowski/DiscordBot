package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface SlashCommandGame {

    void start(@NotNull SlashCommandInteractionEvent event);
}

package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.model.SlashCommand;

public interface SlashCommandGame extends SlashCommand {

    void start(@NotNull SlashCommandInteractionEvent event);
}

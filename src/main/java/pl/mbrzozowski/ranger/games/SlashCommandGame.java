package pl.mbrzozowski.ranger.games;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import pl.mbrzozowski.ranger.model.SlashCommand;

public interface SlashCommandGame extends SlashCommand {

    void start(SlashCommandInteractionEvent event);
}

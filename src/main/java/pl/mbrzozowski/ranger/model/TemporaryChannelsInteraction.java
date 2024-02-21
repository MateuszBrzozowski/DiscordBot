package pl.mbrzozowski.ranger.model;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface TemporaryChannelsInteraction extends TemporaryChannels {
    void removeChannelAfterButtonClick(@NotNull ButtonInteractionEvent event);
}

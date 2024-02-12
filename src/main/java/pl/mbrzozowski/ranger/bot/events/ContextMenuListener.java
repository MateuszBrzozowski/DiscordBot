package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.games.reputation.ReputationService;
import pl.mbrzozowski.ranger.guild.ContextCommands;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextMenuListener extends ListenerAdapter {

    private final ReputationService reputationService;

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        log.info("{} - used user context interaction (name={})", event.getUser(), event.getName());
        if (event.getName().equals(ContextCommands.REPUTATION.getName())) {
            reputationService.plus(event);
        }
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        log.info("{} - used message context interaction (name={})", event.getUser(), event.getName());
        if (event.getName().equals(ContextCommands.REPUTATION.getName())) {
            reputationService.plus(event);
        }
    }
}

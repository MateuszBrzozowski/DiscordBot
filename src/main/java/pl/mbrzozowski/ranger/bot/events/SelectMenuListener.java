package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.helpers.ComponentId;

@Slf4j
@Service
public class SelectMenuListener extends ListenerAdapter {

    private final EventsGeneratorService eventsGeneratorService;
    private final GiveawayService giveawayService;
    private final EventsSettingsService eventsSettingsService;

    @Autowired
    public SelectMenuListener(EventsGeneratorService eventsGeneratorService,
                              GiveawayService giveawayService,
                              EventsSettingsService eventsSettingsService) {
        this.eventsGeneratorService = eventsGeneratorService;
        this.giveawayService = giveawayService;
        this.eventsSettingsService = eventsSettingsService;
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        log.info("{} - StringSelectInteractionEvent{ComponentId={}} interaction event", event.getUser(), event.getComponentId());
        if (event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_SELECT_MENU)) {
            giveawayService.selectAnswer(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENT_GENERATOR_SELECT_MENU_PERM)) {
            eventsGeneratorService.selectAnswer(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENT_SETTINGS_SELECT_MENU)) {
            eventsSettingsService.selectAnswer(event);
        }
    }
}

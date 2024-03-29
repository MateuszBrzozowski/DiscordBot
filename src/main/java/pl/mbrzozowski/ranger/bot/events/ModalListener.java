package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.games.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.recruit.RecruitOpinions;

@Slf4j
@Service
public class ModalListener extends ListenerAdapter {

    private final GiveawayService giveawayService;
    private final EventsGeneratorService eventsGeneratorService;
    private final EventsSettingsService eventsSettingsService;

    public ModalListener(GiveawayService giveawayService,
                         EventsGeneratorService eventsGeneratorService,
                         EventsSettingsService eventsSettingsService) {
        this.giveawayService = giveawayService;
        this.eventsGeneratorService = eventsGeneratorService;
        this.eventsSettingsService = eventsSettingsService;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        log.info("{} - Modal{modalId={}} interaction event", event.getUser(), event.getModalId());
        if (event.getModalId().equalsIgnoreCase(ComponentId.MODAL_RECRUIT_OPINION)) {
            RecruitOpinions.getInstance().submitOpinionAboutRecruit(event);
        } else if (event.getModalId().equalsIgnoreCase(ComponentId.MODAL_COMPLAINTS)) {
            RecruitOpinions.getInstance().submitAnonymousComplaints(event);
        } else if (event.getModalId().equals(ComponentId.GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD) ||
                event.getModalId().equals(ComponentId.GIVEAWAY_GENERATOR_MODAL_RULES_LINK)) {
            giveawayService.generatorSaveAnswer(event);
        } else if (event.getModalId().equalsIgnoreCase(ComponentId.EVENT_GENERATOR_MODAL_TITLE) ||
                event.getModalId().equalsIgnoreCase(ComponentId.EVENT_GENERATOR_MODAL_TIME)) {
            eventsGeneratorService.generatorSaveAnswer(event);
        }else if (event.getModalId().equalsIgnoreCase(ComponentId.EVENT_SETTINGS_MODAL_TITLE) ||
                event.getModalId().equalsIgnoreCase(ComponentId.EVENT_SETTINGS_MODAL_DATE_TIME)) {
            eventsSettingsService.generatorSaveAnswer(event);
        }
    }
}

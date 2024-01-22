package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsSettings;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedInfo;

@Slf4j
public class EventsSettingsCmd extends Proccess {

    private final EventService eventService;
    private final EventsSettingsService eventsSettingsService;

    public EventsSettingsCmd(EventService eventService,
                             EventsSettingsService eventsSettingsService) {
        this.eventService = eventService;
        this.eventsSettingsService = eventsSettingsService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        int indexOfSettings = eventsSettingsService.userHaveActiveSettingsPanel(event.getAuthor().getId());

        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.EVENTS_SETTINGS)) {
            if (indexOfSettings >= 0) {
                eventsSettingsService.removeSettingsPanel(indexOfSettings);
                log.info("{} - removed events settings", event.getAuthor());
            }
            if (eventService.isActiveEvents()) {
                EventsSettings eventsSettings = new EventsSettings(eventService, event, eventsSettingsService);
                eventsSettingsService.addEventsSettings(eventsSettings);
                log.info("{} - Created new events settings", event.getAuthor());
            } else {
                EmbedInfo.noActiveEvents(event.getChannel());
                log.info("{} - No active events. Settings not opened", event.getAuthor());
            }
        } else if (indexOfSettings >= 0) {
            if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.CANCEL) || !eventsSettingsService.isPossibleEditing(indexOfSettings)) {
                EmbedInfo.cancelEventEditing(event.getAuthor().getId());
                eventsSettingsService.removeSettingsPanel(indexOfSettings);
                log.info("{} - Removed events settings", event.getAuthor());
            } else {
                eventsSettingsService.saveAnswerAndNextStage(indexOfSettings, event);
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

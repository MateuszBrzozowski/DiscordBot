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

    public EventsSettingsCmd(MessageReceivedEvent messageReceived,
                             EventService eventService,
                             EventsSettingsService eventsSettingsService) {
        super(messageReceived);
        this.eventService = eventService;
        this.eventsSettingsService = eventsSettingsService;
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        int indexOfSettings = eventsSettingsService.userHaveActiveSettingsPanel(message.getUserID());

        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.EVENTS_SETTINGS)) {
            if (indexOfSettings >= 0) {
                eventsSettingsService.removeSettingsPanel(indexOfSettings);
                log.info("{} - removed events settings", messageReceived.getAuthor());
            }
            if (eventService.isActiveEvents()) {
                EventsSettings eventsSettings = new EventsSettings(eventService, messageReceived, eventsSettingsService);
                eventsSettingsService.addEventsSettings(eventsSettings);
                log.info("{} - Created new events settings", messageReceived.getAuthor());
            } else {
                EmbedInfo.noActiveEvents(messageReceived.getChannel());
                log.info("{} - No active events. Settings not opened", messageReceived.getAuthor());
            }
        } else if (indexOfSettings >= 0) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.CANCEL) || !eventsSettingsService.isPossibleEditing(indexOfSettings)) {
                EmbedInfo.cancelEventEditing(messageReceived.getAuthor().getId());
                eventsSettingsService.removeSettingsPanel(indexOfSettings);
                log.info("{} - Removed events settings", messageReceived.getAuthor());
            } else {
                eventsSettingsService.saveAnswerAndNextStage(indexOfSettings, messageReceived);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

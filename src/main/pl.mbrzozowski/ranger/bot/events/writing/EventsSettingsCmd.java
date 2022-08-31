package ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;
import ranger.embed.EmbedInfo;
import ranger.event.EventService;
import ranger.event.EventsSettings;
import ranger.event.EventsSettingsModel;
import ranger.helpers.Commands;

public class EventsSettingsCmd extends Proccess {

    private final EventService eventService;

    public EventsSettingsCmd(MessageReceivedEvent messageReceived, EventService eventService) {
        super(eventService, messageReceived);
        this.eventService = eventService;
    }

    @Override
    public void proccessMessage(Message message) {
        EventsSettingsModel eventsSettingsModel = Repository.getEventsSettingsModel();
        int indexOfSettings = eventsSettingsModel.userHaveActiveSettingsPanel(message.getUserID());

        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.EVENTS_SETTINGS)) {
            if (indexOfSettings >= 0) {
                eventsSettingsModel.removeSettingsPanel(indexOfSettings);
            }
            if (eventService.isActiveEvents()) {
                EventsSettings eventsSettings = new EventsSettings(eventService, messageReceived);
                eventsSettingsModel.addEventsSettings(eventsSettings);
            } else {
                EmbedInfo.noActiveEvents(messageReceived.getTextChannel());
            }
        } else if (indexOfSettings >= 0) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.CANCEL) || !eventsSettingsModel.isPossibleEditing(indexOfSettings)) {
                EmbedInfo.cancelEventEditing(messageReceived.getAuthor().getId());
                eventsSettingsModel.removeSettingsPanel(indexOfSettings);
            } else {
                eventsSettingsModel.saveAnswerAndNextStage(indexOfSettings, messageReceived);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

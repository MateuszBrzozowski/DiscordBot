package ranger.bot.events.writing;

import ranger.embed.EmbedInfo;
import ranger.event.EventService;
import ranger.event.EventsSettings;
import ranger.event.EventsSettingsModel;
import ranger.helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;

public class EventsSettingsCmd extends Proccess {

    public EventsSettingsCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        EventsSettingsModel eventsSettingsModel = Repository.getEventsSettingsModel();
        EventService event = Repository.getEvent();
        event.isActiveEvents();
        int indexOfSettings = eventsSettingsModel.userHaveActiveSettingsPanel(message.getUserID());

        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.EVENTS_SETTINGS)) {
            if (indexOfSettings >= 0) {
                eventsSettingsModel.removeSettingsPanel(indexOfSettings);
            }
            if (event.isActiveEvents()) {
                EventsSettings eventsSettings = new EventsSettings(messageReceived);
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
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.TIME)) {
            getEvents().changeTime(message.getWords()[1], message.getWords()[2], message.getUserID(), true);
        } else if (message.getWords().length == 4 && message.getWords()[0].equalsIgnoreCase(Commands.TIME) && message.getWords()[3].equalsIgnoreCase(Commands.NO_NOTIFI)) {
            getEvents().changeTime(message.getWords()[1], message.getWords()[2], message.getUserID(), false);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.DATE)) {
            getEvents().changeDate(message.getWords()[1], message.getWords()[2], message.getUserID(), true);
        } else if (message.getWords().length == 4 && message.getWords()[0].equalsIgnoreCase(Commands.DATE) && message.getWords()[3].equalsIgnoreCase(Commands.NO_NOTIFI)) {
            getEvents().changeDate(message.getWords()[1], message.getWords()[2], message.getUserID(), false);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.CANCEL_EVENT)) {
            getEvents().cancelEvnetWithInfoForPlayers(message.getWords()[1]);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.CANCEL_EVENT) && message.getWords()[2].equalsIgnoreCase(Commands.NO_NOTIFI)) {
            getEvents().cancelEvent(message.getWords()[1]);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

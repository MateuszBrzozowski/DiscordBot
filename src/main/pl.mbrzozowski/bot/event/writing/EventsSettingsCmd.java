package bot.event.writing;

import event.EventsSettings;
import event.EventsSettingsModel;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import ranger.Repository;

public class EventsSettingsCmd extends Proccess {

    private PrivateMessageReceivedEvent privateEvent;

    public EventsSettingsCmd(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        EventsSettingsModel eventsSettingsModel = Repository.getEventsSettingsModel();
        int indexOfSettings = eventsSettingsModel.userHaveActiveSettingsPanel(message.getUserID());

        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.EVENTS_SETTINGS)) {
            if (indexOfSettings >= 0) {
                eventsSettingsModel.removeSettingsPanel(indexOfSettings);
            }
            EventsSettings eventsSettings = new EventsSettings(privateEvent);
            eventsSettingsModel.addEventsSettings(eventsSettings);
        } else if (indexOfSettings >= 0) {
            eventsSettingsModel.saveAnswerAndNextStage(indexOfSettings,privateEvent);
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

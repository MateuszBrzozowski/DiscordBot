package bot.event.writing;

import helpers.Commands;

public class EventsSettingsCmd extends Proccess {

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.TIME)) {
            getEvents().changeTime(message.getWords()[1], message.getWords()[2], message.getUserID());
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.DATE)) {
            getEvents().changeDate(message.getWords()[1], message.getWords()[2], message.getUserID());
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.DELETE_EVENT)) {
            getEvents().removeEvent(message.getWords()[1]);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

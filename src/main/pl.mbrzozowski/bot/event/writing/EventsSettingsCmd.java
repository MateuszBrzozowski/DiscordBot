package bot.event.writing;

import helpers.Commands;

public class EventsSettingsCmd extends Proccess {

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.TIME)) {
            getEvents().changeTime(message.getWords()[1], message.getWords()[2], message.getUserID(), true);
        } else if (message.getWords().length == 4 && message.getWords()[0].equalsIgnoreCase(Commands.TIME) && message.getWords()[3].equalsIgnoreCase(Commands.NO_NOTIFI)) {
            getEvents().changeTime(message.getWords()[1], message.getWords()[2], message.getUserID(), false);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.DATE)) {
            getEvents().changeDate(message.getWords()[1], message.getWords()[2], message.getUserID(), true);
        } else if (message.getWords().length == 4 && message.getWords()[0].equalsIgnoreCase(Commands.DATE) && message.getWords()[3].equalsIgnoreCase(Commands.NO_NOTIFI)) {
            getEvents().changeDate(message.getWords()[1], message.getWords()[2], message.getUserID(),false);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.CANCEL_EVENT)) {
            getEvents().cancelEvnetWithInfoForPlayers(message.getWords()[1]);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.CANCEL_EVENT) && message.getWords()[2].equalsIgnoreCase(Commands.NO_NOTIFI)) {
            getEvents().cancelEvent(message.getWords()[1]);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

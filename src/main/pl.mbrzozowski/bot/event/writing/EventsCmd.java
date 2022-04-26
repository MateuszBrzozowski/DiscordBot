package bot.event.writing;

import helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EventsCmd extends Proccess {

    public EventsCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords()[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            String userID = message.getUserID();
            if (message.getWords().length == 4) {
                getEvents().createNewEventFrom3Data(message.getWords(), userID);
            } else if (message.getWords().length == 5) {
                getEvents().createNewEventFrom4Data(message.getWords(), userID);
            } else if (message.getWords().length >= 7) {
                getEvents().createNewEventFromSpecificData(message.getWords(), userID, null);
            }
        } else if (message.getWords()[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            String userID = message.getUserID();
            if (messageReceived != null) {
                if (message.getWords().length == 4) {
                    getEvents().createNewEventFrom3DataHere(message.getWords(), userID, messageReceived.getTextChannel());
                } else if (message.getWords().length == 5) {
                    getEvents().createNewEventFrom4DataHere(message.getWords(), userID, messageReceived.getTextChannel());
                } else if (message.getWords().length >= 7) {
                    getEvents().createNewEventFromSpecificData(message.getWords(), userID, messageReceived.getTextChannel());
                }
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

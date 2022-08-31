package ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.event.EventService;

public class CheckUser extends Proccess {

    public CheckUser(MessageReceivedEvent messageReceived, EventService eventService) {
        super(eventService, messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.isClanMember()) {
            getNextProccess().proccessMessage(message);
        }
    }
}

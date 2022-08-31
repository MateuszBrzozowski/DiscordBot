package ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.event.EventService;

public class CheckUserAdmin extends Proccess {

    private final EventService eventService;

    public CheckUserAdmin(MessageReceivedEvent messageReceived, EventService eventService) {
        super(eventService, messageReceived);
        this.eventService = eventService;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.isAdmin()) {
            getNextProccess().proccessMessage(message);
        } else {
            if (messageReceived.isFromType(ChannelType.PRIVATE) && !messageReceived.getAuthor().isBot()) {
                InvalidCmd invalidCmd = new InvalidCmd(messageReceived, eventService);
                invalidCmd.proccessMessage(message);
            }
        }
    }
}

package ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.event.EventService;
import ranger.helpers.Commands;

public class DeveloperCmd extends Proccess {

    private final EventService eventService;

    public DeveloperCmd(MessageReceivedEvent messageReceived, EventService eventService) {
        super(messageReceived);
        this.eventService = eventService;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            eventService.disableButtons(message.getWords()[1]);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            eventService.disableButtons(message.getWords()[1], message.getWords()[2]);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            eventService.enableButtons(message.getWords()[1]);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            eventService.enableButtons(message.getWords()[1], message.getWords()[2]);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.MSG)) {
            getBotWriter().setChannelID(message.getWords()[1]);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.MSG_CANCEL)) {
            getBotWriter().setActive(false);
        } else if (getBotWriter().isActive()) {
            getBotWriter().sendMsg(messageReceived.getMessage().getContentDisplay());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

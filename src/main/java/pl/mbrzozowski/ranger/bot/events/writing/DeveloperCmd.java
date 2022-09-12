package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.model.BotWriter;

public class DeveloperCmd extends Proccess {

    private final EventService eventService;
    private final BotWriter botWriter;

    public DeveloperCmd(MessageReceivedEvent messageReceived, EventService eventService, BotWriter botWriter) {
        super(messageReceived);
        this.eventService = eventService;
        this.botWriter = botWriter;
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
            botWriter.setChannelID(message.getWords()[1]);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.MSG_CANCEL)) {
            botWriter.setActive(false);
        } else if (botWriter.isActive()) {
            botWriter.sendMsg(messageReceived.getMessage().getContentDisplay());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

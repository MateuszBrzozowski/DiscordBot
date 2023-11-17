package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.model.BotWriter;

@Slf4j
public class DeveloperCmd extends Proccess {

    private final EventService eventService;
    private final BotWriter botWriter;

    public DeveloperCmd(MessageReceivedEvent messageReceived, EventService eventService, BotWriter botWriter) {
        super(messageReceived);
        this.eventService = eventService;
        this.botWriter = botWriter;
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            eventService.disableButtons(message.getWords()[1]);
            log.info("{} - disable buttons (msg={})", messageReceived.getAuthor(), message.getContentDisplay());
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            eventService.disableButtons(message.getWords()[1], message.getWords()[2]);
            log.info("{} - disable buttons (msg={})", messageReceived.getAuthor(), message.getContentDisplay());
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            eventService.enableButtons(message.getWords()[1]);
            log.info("{} - enable buttons (msg={})", messageReceived.getAuthor(), message.getContentDisplay());
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            eventService.enableButtons(message.getWords()[1], message.getWords()[2]);
            log.info("{} - enable buttons (msg={})", messageReceived.getAuthor(), message.getContentDisplay());
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.MSG)) {
            botWriter.setChannelID(message.getWords()[1]);
            log.info("{} - set channel id for send message", messageReceived.getAuthor());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.MSG_CANCEL)) {
            botWriter.setActive(false);
            log.info("{} - cancel sending message", messageReceived.getAuthor());
        } else if (botWriter.isActive()) {
            botWriter.sendMsg(messageReceived.getMessage().getContentDisplay());
            log.info("{} - send message as RangerBot", messageReceived.getAuthor());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

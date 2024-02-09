package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.guild.Commands;
import pl.mbrzozowski.ranger.model.BotWriter;

@Slf4j
public class DeveloperCmd extends Proccess {

    private final EventService eventService;
    private final BotWriter botWriter;

    public DeveloperCmd(EventService eventService, BotWriter botWriter) {
        this.eventService = eventService;
        this.botWriter = botWriter;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        String[] words = event.getMessage().getContentRaw().split(" ");
        if (words.length == 2 && words[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            eventService.disableButtons(words[1]);
            log.info("{} - disable buttons (msg={})", event.getAuthor(), event.getMessage().getContentRaw());
        } else if (words.length == 3 && words[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            eventService.disableButtons(words[1], words[2]);
            log.info("{} - disable buttons (msg={})", event.getAuthor(), event.getMessage().getContentRaw());
        } else if (words.length == 2 && words[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            eventService.enableButtons(words[1]);
            log.info("{} - enable buttons (msg={})", event.getAuthor(), event.getMessage().getContentRaw());
        } else if (words.length == 3 && words[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            eventService.enableButtons(words[1], words[2]);
            log.info("{} - enable buttons (msg={})", event.getAuthor(), event.getMessage().getContentRaw());
        } else if (words.length == 2 && words[0].equalsIgnoreCase(Commands.MSG)) {
            botWriter.setChannelID(words[1]);
            log.info("{} - set channel id for send message", event.getAuthor());
        } else if (words.length == 1 && words[0].equalsIgnoreCase(Commands.MSG_CANCEL)) {
            botWriter.setActive(false);
            log.info("{} - cancel sending message", event.getAuthor());
        } else if (botWriter.isActive()) {
            botWriter.sendMsg(event.getMessage().getContentDisplay());
            log.info("{} - send message as RangerBot", event.getAuthor());
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

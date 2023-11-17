package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGenerator;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedInfo;

@Slf4j
public class GeneratorCmd extends Proccess {

    private final EventService eventService;
    private final EventsGeneratorService eventsGeneratorService;

    @Autowired
    public GeneratorCmd(MessageReceivedEvent messageReceived,
                        EventService eventService,
                        EventsGeneratorService eventsGeneratorService) {
        super(messageReceived);
        this.eventService = eventService;
        this.eventsGeneratorService = eventsGeneratorService;
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        int indexOfGenerator = eventsGeneratorService.userHaveActiveGenerator(message.getUserID());
        if (message.getContentDisplay().equalsIgnoreCase(Commands.EVENT)) {
            if (messageReceived.isFromType(ChannelType.PRIVATE)) {
                if (messageReceived != null) {
                    String authorID = messageReceived.getAuthor().getId();
                    if (indexOfGenerator == -1) {
                        EventsGenerator eventsGenerator = new EventsGenerator(messageReceived, eventService, eventsGeneratorService);
                        eventsGeneratorService.addEventsGenerator(eventsGenerator);
                        log.info(messageReceived.getAuthor() + " - Created new event generator");
                    } else {
                        EmbedInfo.userHaveActiveEventGenerator(authorID);
                        EmbedInfo.cancelEventGenerator(message.getUserID());
                        EmbedInfo.createNewGenerator(authorID);
                        eventsGeneratorService.removeGenerator(indexOfGenerator);
                        EventsGenerator eventsGenerator = new EventsGenerator(messageReceived, eventService, eventsGeneratorService);
                        eventsGeneratorService.addEventsGenerator(eventsGenerator);
                        log.info(messageReceived.getAuthor() + " - Had active event generator. Created new event generator");
                    }
                }
            }
        } else if (indexOfGenerator >= 0 && messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.CANCEL)) {
                EmbedInfo.cancelEventGenerator(message.getUserID());
                eventsGeneratorService.removeGenerator(indexOfGenerator);
                log.info(messageReceived.getAuthor() + " - canceled event generator");
            } else {
                eventsGeneratorService.saveAnswerAndNextStage(messageReceived, indexOfGenerator);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

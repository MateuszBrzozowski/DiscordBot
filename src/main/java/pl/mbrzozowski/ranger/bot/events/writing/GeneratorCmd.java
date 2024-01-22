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
    public GeneratorCmd(EventService eventService,
                        EventsGeneratorService eventsGeneratorService) {
        this.eventService = eventService;
        this.eventsGeneratorService = eventsGeneratorService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        int indexOfGenerator = eventsGeneratorService.userHaveActiveGenerator(event.getAuthor().getId());
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.EVENT)) {
            if (event.isFromType(ChannelType.PRIVATE)) {
                String authorID = event.getAuthor().getId();
                if (indexOfGenerator == -1) {
                    EventsGenerator eventsGenerator = new EventsGenerator(event, eventService, eventsGeneratorService);
                    eventsGeneratorService.addEventsGenerator(eventsGenerator);
                    log.info(event.getAuthor() + " - Created new event generator");
                } else {
                    EmbedInfo.userHaveActiveEventGenerator(authorID);
                    EmbedInfo.cancelEventGenerator(event.getAuthor().getId());
                    EmbedInfo.createNewGenerator(authorID);
                    eventsGeneratorService.removeGenerator(indexOfGenerator);
                    EventsGenerator eventsGenerator = new EventsGenerator(event, eventService, eventsGeneratorService);
                    eventsGeneratorService.addEventsGenerator(eventsGenerator);
                    log.info(event.getAuthor() + " - Had active event generator. Created new event generator");
                }
            }
        } else if (indexOfGenerator >= 0 && event.isFromType(ChannelType.PRIVATE)) {
            if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.CANCEL)) {
                EmbedInfo.cancelEventGenerator(event.getAuthor().getId());
                eventsGeneratorService.removeGenerator(indexOfGenerator);
                log.info(event.getAuthor() + " - canceled event generator");
            } else {
                eventsGeneratorService.saveAnswerAndNextStage(event, indexOfGenerator);
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

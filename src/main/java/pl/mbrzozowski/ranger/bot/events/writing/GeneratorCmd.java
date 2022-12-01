package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGenerator;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.helpers.Commands;

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
    public void proccessMessage(Message message) {
        int indexOfGenerator = eventsGeneratorService.userHaveActiveGenerator(message.getUserID());
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.GENERATOR)) {
            if (messageReceived.isFromType(ChannelType.PRIVATE)) {
                if (messageReceived != null) {
                    String authorID = messageReceived.getAuthor().getId();
                    if (indexOfGenerator == -1) {
                        EventsGenerator eventsGenerator = new EventsGenerator(messageReceived, eventService, eventsGeneratorService);
                        eventsGeneratorService.addEventsGenerator(eventsGenerator);
                    } else {
                        EmbedInfo.userHaveActiveEventGenerator(authorID);
                        EmbedInfo.cancelEventGenerator(message.getUserID());
                        EmbedInfo.createNewGenerator(authorID);
                        eventsGeneratorService.removeGenerator(indexOfGenerator);
                        EventsGenerator eventsGenerator = new EventsGenerator(messageReceived, eventService, eventsGeneratorService);
                        eventsGeneratorService.addEventsGenerator(eventsGenerator);
                    }
                }
            }
        } else if (indexOfGenerator >= 0 && messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.CANCEL)) {
                EmbedInfo.cancelEventGenerator(message.getUserID());
                eventsGeneratorService.removeGenerator(indexOfGenerator);
            } else {
                eventsGeneratorService.saveAnswerAndNextStage(messageReceived, indexOfGenerator);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

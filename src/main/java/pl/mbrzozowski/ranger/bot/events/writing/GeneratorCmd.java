package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.Repository;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.event.EventsGenerator;
import pl.mbrzozowski.ranger.event.EventsGeneratorModel;

public class GeneratorCmd extends Proccess {

    private final EventService eventService;

    public GeneratorCmd(MessageReceivedEvent messageReceived, EventService eventService) {
        super(messageReceived);
        this.eventService = eventService;
    }

    @Override
    public void proccessMessage(Message message) {
        EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
        int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(message.getUserID());
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.GENERATOR)) {
            if (messageReceived.isFromType(ChannelType.PRIVATE)) {
                if (messageReceived != null) {
                    String authorID = messageReceived.getAuthor().getId();
                    if (indexOfGenerator == -1) {
                        EventsGenerator eventsGenerator = new EventsGenerator(messageReceived, eventService);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    } else {
                        EmbedInfo.userHaveActiveEventGenerator(authorID);
                        EmbedInfo.cancelEventGenerator(message.getUserID());
                        EmbedInfo.createNewGenerator(authorID);
                        eventsGeneratorModel.removeGenerator(indexOfGenerator);
                        EventsGenerator eventsGenerator = new EventsGenerator(messageReceived, eventService);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    }
                }
            }
        } else if (indexOfGenerator >= 0 && messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.CANCEL)) {
                EmbedInfo.cancelEventGenerator(message.getUserID());
                eventsGeneratorModel.removeGenerator(indexOfGenerator);
            } else {
                eventsGeneratorModel.saveAnswerAndNextStage(messageReceived, indexOfGenerator);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
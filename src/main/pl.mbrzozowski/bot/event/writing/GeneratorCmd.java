package bot.event.writing;

import embed.EmbedInfo;
import event.EventsGenerator;
import event.EventsGeneratorModel;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;

public class GeneratorCmd extends Proccess {

    public GeneratorCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
        int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(message.getUserID());
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.GENERATOR)) {
            if (messageReceived != null) {
                messageReceived.getMessage().delete().submit();
                String authorID = messageReceived.getAuthor().getId();
                if (indexOfGenerator == -1) {
                    EventsGenerator eventsGenerator = new EventsGenerator(messageReceived);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                } else {
                    EmbedInfo.userHaveActiveEventGenerator(authorID);
                    EmbedInfo.cancelEventGenerator(message.getUserID());
                    EmbedInfo.createNewGenerator(authorID);
                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
                    EventsGenerator eventsGenerator = new EventsGenerator(messageReceived);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                }
            }
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.GENERATOR_HERE)) {
            if (messageReceived != null) {
                messageReceived.getMessage().delete().submit();
                String authorID = messageReceived.getAuthor().getId();
                if (indexOfGenerator == -1) {
                    EventsGenerator eventsGenerator = new EventsGenerator(messageReceived);
                    eventsGenerator.setSpecificChannel(true);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                } else {
                    EmbedInfo.userHaveActiveEventGenerator(authorID);
                    EmbedInfo.cancelEventGenerator(message.getUserID());
                    EmbedInfo.createNewGenerator(authorID);
                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
                    EventsGenerator eventsGenerator = new EventsGenerator(messageReceived);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                }
            }
        } else if (indexOfGenerator >= 0 && messageReceived != null) {
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

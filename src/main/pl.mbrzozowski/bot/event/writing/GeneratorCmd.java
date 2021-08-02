package bot.event.writing;

import embed.EmbedInfo;
import event.EventsGenerator;
import event.EventsGeneratorModel;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import ranger.Repository;

public class GeneratorCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;
    private PrivateMessageReceivedEvent privateEvent;

    public GeneratorCmd(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    public GeneratorCmd(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1) {
            EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
            if (message.getWords()[0].equalsIgnoreCase(Commands.GENERATOR)) {
                if (guildEvent != null) {
                    guildEvent.getMessage().delete().submit();
                    String authorID = guildEvent.getAuthor().getId();
                    int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
                    if (indexOfGenerator == -1) {
                        EventsGenerator eventsGenerator = new EventsGenerator(guildEvent);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    } else {
                        EmbedInfo.userHaveActiveEventGenerator(authorID);
                        eventsGeneratorModel.cancelEventGenerator(guildEvent);
                        EmbedInfo.createNewGenerator(authorID);
                        eventsGeneratorModel.removeGenerator(indexOfGenerator);
                        EventsGenerator eventsGenerator = new EventsGenerator(guildEvent);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    }
                } else if (privateEvent != null) {
                    String authorID = privateEvent.getAuthor().getId();
                    int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
                    if (eventsGeneratorModel.userHaveActiveGenerator(authorID) == -1) {
                        EventsGenerator eventsGenerator = new EventsGenerator(privateEvent);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    } else {
                        EmbedInfo.userHaveActiveEventGenerator(authorID);
                        eventsGeneratorModel.cancelEventGenerator(privateEvent);
                        EmbedInfo.createNewGenerator(authorID);
                        eventsGeneratorModel.removeGenerator(indexOfGenerator);
                        EventsGenerator eventsGenerator = new EventsGenerator(privateEvent);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    }
                }
            } else if (message.getWords()[0].equalsIgnoreCase(Commands.GENERATOR_HERE)) {
                if (guildEvent != null) {
                    guildEvent.getMessage().delete().submit();
                    String authorID = guildEvent.getAuthor().getId();
                    int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
                    if (indexOfGenerator == -1) {
                        EventsGenerator eventsGenerator = new EventsGenerator(guildEvent);
                        eventsGenerator.setHere(true);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    } else {
                        EmbedInfo.userHaveActiveEventGenerator(authorID);
                        eventsGeneratorModel.cancelEventGenerator(guildEvent);
                        EmbedInfo.createNewGenerator(authorID);
                        eventsGeneratorModel.removeGenerator(indexOfGenerator);
                        EventsGenerator eventsGenerator = new EventsGenerator(guildEvent);
                        eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                    }
                }
            } else if (message.getWords()[0].equalsIgnoreCase(Commands.CANCEL)) {
                if (privateEvent != null) {
                    int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(message.getUserID());
                    if (indexOfGenerator >= 0) {
                        eventsGeneratorModel.cancelEventGenerator(privateEvent);
                        eventsGeneratorModel.removeGenerator(indexOfGenerator);
                    } else {
                        eventsGeneratorModel.saveAnswerAndNextStage(privateEvent, indexOfGenerator);
                    }
                }
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }

    }
}

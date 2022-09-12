package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class EventsGeneratorModel {

    private List<EventsGenerator> eventsGenerators = new ArrayList<>();

    public void addEventsGenerator(EventsGenerator eGM) {
        eventsGenerators.add(eGM);
    }

    public int userHaveActiveGenerator(String authorID) {
        if (!eventsGenerators.isEmpty()) {
            for (int i = 0; i < eventsGenerators.size(); i++) {
                if (eventsGenerators.get(i).getUserID().equalsIgnoreCase(authorID)) {
                    return i;
                }
            }
        }
        return -1;

    }

    public void saveAnswerAndNextStage(MessageReceivedEvent event, int indexOfGenerator) {
        eventsGenerators.get(indexOfGenerator).saveAnswerAndSetNextStage(event);
    }

    public void saveAnswerAndNextStage(ButtonInteractionEvent buttonAnswer, int indexOfGenerator) {
        eventsGenerators.get(indexOfGenerator).saveAnswerAndSetNextStage(buttonAnswer);
    }

    public void saveAnswerAndNextStage(SelectMenuInteractionEvent event, int indexOfGenerator) {
        eventsGenerators.get(indexOfGenerator).saveAnswerAndSetNextStage(event);
    }

    public void removeGenerator(int indexOfGenerator) {
        eventsGenerators.remove(indexOfGenerator);
    }
}

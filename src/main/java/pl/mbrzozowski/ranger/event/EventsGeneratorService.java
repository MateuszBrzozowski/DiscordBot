package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EventsGeneratorService {

    private final List<EventsGenerator> eventsGenerators = new ArrayList<>();

    public int userHaveActiveGenerator(String authorID) {
        if (eventsGenerators.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < eventsGenerators.size(); i++) {
            if (eventsGenerators.get(i).getUserID().equalsIgnoreCase(authorID)) {
                return i;
            }
        }
        return -1;
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        int index = userHaveActiveGenerator(event.getUser().getId());
        if (index == -1) {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            return;
        }
        eventsGenerators.get(index).buttonEvent(event);
    }

    public void selectAnswer(@NotNull StringSelectInteractionEvent event) {
        event.getInteraction().deferEdit().queue();
        int index = userHaveActiveGenerator(event.getUser().getId());
        eventsGenerators.get(index).selectAnswer(event);
    }

    public void removeGenerator(int indexOfGenerator) {
        eventsGenerators.remove(indexOfGenerator);
    }

    public void removeGenerator(String userId) {
        for (int i = 0; i < eventsGenerators.size(); i++) {
            if (eventsGenerators.get(i).getUserID().equals(userId)) {
                eventsGenerators.remove(i);
                log.info("Removed generator for userId={}", userId);
                return;
            }
        }
    }

    public void generatorSaveAnswer(@NotNull ModalInteractionEvent event) {
        int index = userHaveActiveGenerator(event.getUser().getId());
        eventsGenerators.get(index).submit(event);
    }

    public void createGenerator(@NotNull MessageReceivedEvent event, EventService eventService) {
        int index = userHaveActiveGenerator(event.getAuthor().getId());
        if (event.isFromType(ChannelType.PRIVATE)) {
            if (index == -1) {
                EventsGenerator eventsGenerator = new EventsGenerator(event, eventService, this);
                eventsGenerators.add(eventsGenerator);
                log.info(event.getAuthor() + " - Created new event generator");
            } else {
                eventsGenerators.get(index).cancel();
                removeGenerator(index);
                EventsGenerator eventsGenerator = new EventsGenerator(event, eventService, this);
                eventsGenerators.add(eventsGenerator);
                log.info(event.getAuthor() + " - Had active event generator. Created new event generator");
            }
        }
    }
}

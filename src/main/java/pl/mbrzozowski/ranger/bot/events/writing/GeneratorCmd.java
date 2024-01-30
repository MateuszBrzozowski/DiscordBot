package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.helpers.Commands;

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
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.EVENT)) {
            if (eventService.isMaxEvents()) {
                event.getMessage().reply("Osiągnięto maksymalną liczbę eventów").queue();
            } else {
                eventsGeneratorService.createGenerator(event, eventService);
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

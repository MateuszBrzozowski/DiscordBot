package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.helpers.Commands;

@Slf4j
public class EventsSettingsCmd extends Proccess {

    private final EventsSettingsService eventsSettingsService;

    public EventsSettingsCmd(EventsSettingsService eventsSettingsService) {
        this.eventsSettingsService = eventsSettingsService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.EVENTS_SETTINGS)) {
            eventsSettingsService.createSettings(event);
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.guild.ComponentId;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EventsSettingsService {

    private final List<EventsSettings> eventsSettings = new ArrayList<>();
    private final EventService eventService;

    public EventsSettingsService(EventService eventService) {
        this.eventService = eventService;
    }

    public int userHaveActiveSettingsPanel(String userID) {
        if (!eventsSettings.isEmpty()) {
            for (int i = 0; i < eventsSettings.size(); i++) {
                if (eventsSettings.get(i).getUserId().equalsIgnoreCase(userID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void removeSettingsPanel(int index) {
        eventsSettings.remove(index);
    }

    public void removeSettingsPanel(String userId) {
        for (int i = 0; i < eventsSettings.size(); i++) {
            if (eventsSettings.get(i).getUserId().equals(userId)) {
                eventsSettings.remove(i);
                log.info("Removed settings panel for userId={}", userId);
                return;
            }
        }
    }

    public void createSettings(@NotNull MessageReceivedEvent event) {
        int index = userHaveActiveSettingsPanel(event.getAuthor().getId());
        if (index == -1) {
            EventsSettings eventsSettings = new EventsSettings(eventService, event, this);
            this.eventsSettings.add(eventsSettings);
            log.info("{} - Created new events settings", event.getAuthor());
        } else {
            this.eventsSettings.get(index).cancel();
            removeSettingsPanel(index);
            log.info("{} - Cancel event settings", event.getAuthor());
            EventsSettings eventsSettings = new EventsSettings(eventService, event, this);
            this.eventsSettings.add(eventsSettings);
            log.info("{} - Created new events settings", event.getAuthor());
        }
    }

    public void createSettings(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Sprawdź wiadomości prywatne").setEphemeral(true).queue();
        int index = userHaveActiveSettingsPanel(event.getUser().getId());
        if (index == -1) {
            EventsSettings eventsSettings = new EventsSettings(eventService, event, this);
            this.eventsSettings.add(eventsSettings);
            log.info("{} - Created new events settings", event.getUser());
        } else {
            this.eventsSettings.get(index).cancel();
            removeSettingsPanel(index);
            log.info("{} - Cancel event settings", event.getUser());
            EventsSettings eventsSettings = new EventsSettings(eventService, event, this);
            this.eventsSettings.add(eventsSettings);
            log.info("{} - Created new events settings", event.getUser());
        }
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        int index = userHaveActiveSettingsPanel(event.getUser().getId());
        if (index == -1) {
            if (event.getComponentId().equals(ComponentId.EVENT_SETTINGS_GO_TO_START)) {
                EventsSettings eventsSettings = new EventsSettings(eventService, event, this);
                this.eventsSettings.add(eventsSettings);
            } else {
                event.deferEdit().queue();
                event.getMessage().delete().queue();
            }
            return;
        }
        eventsSettings.get(index).buttonEvent(event);
    }

    public void selectAnswer(@NotNull StringSelectInteractionEvent event) {
        int index = userHaveActiveSettingsPanel(event.getUser().getId());
        if (index == -1) {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            return;
        }
        eventsSettings.get(index).selectAnswer(event);
    }

    public void generatorSaveAnswer(@NotNull ModalInteractionEvent event) {
        int index = userHaveActiveSettingsPanel(event.getUser().getId());
        eventsSettings.get(index).submit(event);
    }
}

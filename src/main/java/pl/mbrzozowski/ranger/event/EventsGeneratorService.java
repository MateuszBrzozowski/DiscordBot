package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.guild.RangersGuild;

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
        int index = userHaveActiveGenerator(event.getUser().getId());
        if (index == -1) {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            return;
        }
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

    public void createGenerator(@NotNull MessageReceivedEvent event, @NotNull EventService eventService) {
        if (!isPossibleForMessage(event, eventService)) {
            return;
        }
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

    public void createGenerator(@NotNull SlashCommandInteractionEvent event, @NotNull EventService eventService) {
        if (!isPossibleForSlash(event, eventService)) {
            return;
        }
        event.reply("Sprawdź wiadomości  prywatne").setEphemeral(true).queue();
        int index = userHaveActiveGenerator(event.getUser().getId());
        if (index == -1) {
            EventsGenerator eventsGenerator = new EventsGenerator(event, eventService, this);
            eventsGenerators.add(eventsGenerator);
            log.info(event.getUser() + " - Created new event generator");
        } else {
            eventsGenerators.get(index).cancel();
            removeGenerator(index);
            EventsGenerator eventsGenerator = new EventsGenerator(event, eventService, this);
            eventsGenerators.add(eventsGenerator);
            log.info(event.getUser() + " - Had active event generator. Created new event generator");
        }

    }

    private boolean isPossibleForMessage(@NotNull MessageReceivedEvent event, @NotNull EventService eventService) {
        if (eventService.isMaxActiveEvents()) {
            event.getMessage().reply("Osiągnięto maksymalną liczbę aktywnych eventów!").queue();
            log.info("Max active events");
            return false;
        } else if (!eventService.isSpaceInCategory()) {
            event.getMessage().reply("Osiągnięto maksymalną liczbę kanałów w kategorii!").queue();
            log.info("Max channels in category");
            return false;
        } else if (RangersGuild.isNoSpaceOnGuild()) {
            event.getMessage().reply("Osiągnięto maksymalną liczbę kanałów na serwerze!").queue();
            log.info("Max channels on Guild");
            return false;
        }
        return true;
    }

    private boolean isPossibleForSlash(@NotNull SlashCommandInteractionEvent event, @NotNull EventService eventService) {
        if (eventService.isMaxActiveEvents()) {
            event.reply("Osiągnięto maksymalną liczbę aktywnych eventów!").setEphemeral(true).queue();
            log.info("Max active events");
            return false;
        }
        if (!eventService.isSpaceInCategory()) {
            event.reply("Osiągnięto maksymalną liczbę kanałów w kategorii!").setEphemeral(true).queue();
            log.info("Max channels in category");
            return false;
        }
        if (RangersGuild.isNoSpaceOnGuild()) {
            event.reply("Osiągnięto maksymalną liczbę kanałów na serwerze!").setEphemeral(true).queue();
            log.info("Max channels on Guild");
            return false;
        }
        return true;
    }
}

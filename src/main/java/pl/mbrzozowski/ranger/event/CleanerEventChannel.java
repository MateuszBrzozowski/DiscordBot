package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
public class CleanerEventChannel extends CleanerChannel {

    private final EventService eventService;
    private final int delayInDaysForTactical;
    private final int delayInDays;

    @Autowired
    public CleanerEventChannel(EventService eventService, int delay, int delayForTactical) {
        this.eventService = eventService;
        this.delayInDays = delay;
        this.delayInDaysForTactical = delayForTactical;
        log.info("Delay to delete channel for default events(days)={}", delayInDays);
        log.info("Delay to delete channel for tactical events(days)={}", delayInDays);
    }

    @Override
    public void run() {
        log.info("Event cleaning channels init");
        List<Event> eventList = eventService.findAll()
                .stream()
                .filter(event -> event.getDate().isBefore(LocalDateTime.now(ZoneId.of("Europe/Paris"))))
                .toList();
        for (Event event : eventList) {
            if ((event.getDate().isBefore(LocalDateTime.now().minusDays(delayInDays))) ||
                    (event.getDate().isBefore(LocalDateTime.now().minusDays(delayInDaysForTactical)) && event.getEventFor() == EventFor.TACTICAL_GROUP)) {
                eventService.delete(event);
                deleteChannel(event);
            } else {
                eventService.disableButtons(event);
                eventService.setActiveToFalse(event);
                eventService.setRedCircleInChannelName(event);
            }
        }
    }

    private void deleteChannel(@NotNull Event event) {
        TextChannel channel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (channel != null) {
            channel.delete().reason("Upłynął czas utrzymywania kanału").queue();
            log.info("Past event - Deleted channel - [" + event.getName() + "]");
        }
    }
}

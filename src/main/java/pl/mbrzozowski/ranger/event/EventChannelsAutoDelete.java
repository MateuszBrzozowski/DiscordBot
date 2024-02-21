package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
public class EventChannelsAutoDelete extends CleanerChannel {

    private final EventService eventService;
    private final int delayInDaysForTactical;

    @Autowired
    public EventChannelsAutoDelete(EventService eventService, int delay, int delayForTactical) {
        super(delay);
        this.eventService = eventService;
        this.delayInDaysForTactical = delayForTactical;
        log.info("Delay to delete channel for default events(days)={}", delay);
        log.info("Delay to delete channel for tactical events(days)={}", delayForTactical);
    }

    @Override
    public void run() {
        log.info("Event cleaning channels init");
        List<Event> eventList = eventService.findAll()
                .stream()
                .filter(event -> event.getDate().isBefore(LocalDateTime.now(ZoneId.of("Europe/Paris"))))
                .toList();
        for (Event event : eventList) {
            if ((event.getDate().isBefore(LocalDateTime.now().minusDays(delay))) ||
                    (event.getDate().isBefore(LocalDateTime.now().minusDays(delayInDaysForTactical)) && event.getEventFor() == EventFor.TACTICAL_GROUP)) {
                eventService.delete(event);
                eventService.deleteChannelById(event.getChannelId());
            } else {
                eventService.disableButtons(event);
                eventService.setActiveToFalse(event);
                eventService.setRedCircleInChannelName(event);
            }
        }
    }
}

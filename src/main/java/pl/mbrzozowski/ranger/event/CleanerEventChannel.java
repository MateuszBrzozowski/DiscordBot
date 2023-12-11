package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.LocalDate.now;

@Slf4j
@Service
public class CleanerEventChannel extends TimerTask implements CleanerChannel {

    private final EventService eventService;
    private final int DELAY_IN_DAYS;
    private final int DELAY_IN_DAYS_FOR_TACTICAL;

    @Autowired
    public CleanerEventChannel(EventService eventService,
                               @Value("${event.channel.cleaning}") int delay,
                               @Value("${event.tactical.channel.cleaning}") int delayForTactical) {
        this.eventService = eventService;
        this.DELAY_IN_DAYS = delay;
        this.DELAY_IN_DAYS_FOR_TACTICAL = delayForTactical;
        Timer timer = new Timer();
        Date date = new Date(now().getYear() - 1900, now().getMonthValue() - 1, now().getDayOfMonth());
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        timer.scheduleAtFixedRate(this, date, 24 * 60 * 60 * 1000);
    }

    @Override
    public void clean() {
        log.info("Event cleaning channels");
        List<Event> eventList = eventService.findAll()
                .stream()
                .filter(event -> event.getDate().isBefore(LocalDateTime.now(ZoneId.of("Europe/Paris"))))
                .toList();
        for (Event event : eventList) {
            if ((event.getDate().isBefore(LocalDateTime.now().minusDays(DELAY_IN_DAYS))) ||
                    (event.getDate().isBefore(LocalDateTime.now().minusDays(DELAY_IN_DAYS_FOR_TACTICAL)) && event.getEventFor() == EventFor.TACTICAL_GROUP)) {
                eventService.delete(event);
                deleteChannel(event);
            } else {
                eventService.disableButtons(event);
                eventService.setActiveToFalse(event);
                eventService.setRedCircleInChannelName(event);
            }
        }
    }

    @Override
    public void run() {
        clean();
    }

    private void deleteChannel(Event event) {
        TextChannel channel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
        if (channel != null) {
            channel.delete().reason("Upłynął czas utrzymywania kanału").queue();
            log.info("Past event - Deleted channel - [" + event.getName() + "]");
        }
    }
}

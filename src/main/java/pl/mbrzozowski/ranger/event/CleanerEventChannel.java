package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.RangerLogger;
import pl.mbrzozowski.ranger.Repository;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.LocalDate.now;

@Service
public class CleanerEventChannel extends TimerTask implements CleanerChannel {

    private final EventService eventService;
    private static final int DELAY_IN_DAYS = 30;

    @Autowired
    public CleanerEventChannel(EventService eventService) {
        this.eventService = eventService;
        Timer timer = new Timer();
        Date date = new Date(now().getYear() - 1900, now().getMonthValue() - 1, now().getDayOfMonth());
        date.setHours(23);
        date.setMinutes(59);
        timer.scheduleAtFixedRate(this, date, 24 * 60 * 60 * 1000);
    }

    @Override
    public void clean() {
        List<Event> eventList = eventService.findAll()
                .stream()
                .filter(event -> event.getDate().isBefore(LocalDateTime.now()))
                .toList();
        for (Event event : eventList) {
            if (event.getDate().isBefore(LocalDateTime.now().minusDays(DELAY_IN_DAYS))) {
                eventService.delete(event);
                deleteChannel(event);
            } else {
                eventService.changeTitleRedCircle(event);
            }
        }
    }

    @Override
    public void run() {
        clean();
    }

    private void deleteChannel(Event event) {
        TextChannel channel = Repository.getJda().getTextChannelById(event.getChannelId());
        if (channel != null) {
            channel.delete().reason("Upłynął czas utrzymywania kanału").queue();
            RangerLogger.info("Upłynął czas utrzymywania kanału - Usunięto pomyślnie - [" + event.getName() + "]");
        }
    }
}

package pl.mbrzozowski.ranger.event.reminder;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;

@Slf4j
public class CreateReminder {

    private final LocalDateTime eventDateTime;
    private final String eventID;
    private final EventService eventService;
    private final Timers timers;


    public CreateReminder(@NotNull Event event, EventService eventService, Timers timers) {
        this.eventDateTime = event.getDate().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime();
        this.eventID = event.getMsgId();
        this.eventService = eventService;
        this.timers = timers;
    }

    public void create() {
        if (eventDateTime != null) {
            setReminder();
        }
    }

    private void setReminder() {
        if (eventDateTime != null) {
            LocalDateTime oneHourBefore = eventDateTime.minusHours(1);
            oneHourBefore.atZone(ZoneId.of("Europe/Paris"));
            LocalDateTime oneDayBefore = eventDateTime.minusDays(1);
            oneDayBefore.atZone(ZoneId.of("Europe/Paris"));
            setReminderWithExactTime(oneHourBefore, TypeOfReminder.ONE_HOUR);
            setReminderWithExactTime(oneDayBefore, TypeOfReminder.ONE_DAY);
        }
    }

    private void setReminderWithExactTime(LocalDateTime timerTme, TypeOfReminder type) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        Date eventDateTime = Date.from(timerTme.atZone(ZoneId.of("Europe/Paris")).toInstant());
        if (dateTimeNow.isBefore(timerTme)) {
            Timer timer = new Timer();
            timer.schedule(new Reminder(eventID, type, eventService), eventDateTime);
            MyTimer myTimer = new MyTimer(eventID, timer);
            timers.add(myTimer);
        }
    }
}

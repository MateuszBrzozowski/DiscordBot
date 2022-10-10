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
    private final String msgId;
    private final String channelId;
    private final EventService eventService;
    private final Timers timers;
    private final UsersReminderService usersReminderService;


    public CreateReminder(@NotNull Event event,
                          EventService eventService,
                          Timers timers,
                          UsersReminderService usersReminderService) {
        this.eventDateTime = event.getDate().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime();
        this.msgId = event.getMsgId();
        this.channelId = event.getChannelId();
        this.eventService = eventService;
        this.timers = timers;
        this.usersReminderService = usersReminderService;
    }

    public void create() {
        setReminder();
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

    private void setReminderWithExactTime(@NotNull LocalDateTime timerTme, TypeOfReminder type) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        Date eventDateTime = Date.from(timerTme.atZone(ZoneId.of("Europe/Paris")).toInstant());
        if (dateTimeNow.isBefore(timerTme)) {
            Timer timer = new Timer();
            timer.schedule(new Reminder(msgId, type, eventService, usersReminderService), eventDateTime);
            MyTimer myTimer = new MyTimer(msgId, channelId, timer);
            timers.add(myTimer);
        }
    }
}

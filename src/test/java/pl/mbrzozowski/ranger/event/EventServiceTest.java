package pl.mbrzozowski.ranger.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mbrzozowski.ranger.event.reminder.Timers;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.repository.main.EventRepository;

import static org.mockito.Mockito.mock;

class EventServiceTest {

    EventService eventService;

    @BeforeEach
    void beforeEach() {
        EventRepository eventRepository = mock(EventRepository.class);
        Timers timers = mock(Timers.class);
        UsersReminderService usersReminderService = mock(UsersReminderService.class);
        eventService = new EventService(eventRepository, timers, usersReminderService);
    }

    @Test
    void test() {
//        //<t:1675015200:F>
//        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 29, 19, 0);
//        long epochSecond = dateTime.atZone(ZoneId.of("Europe/Paris")).toEpochSecond() * 1000;
//        System.out.println("1675015200");
//        System.out.println(epochSecond);
//
//        System.out.println(TimeFormat.DATE_TIME_LONG.format(epochSecond));
    }
}
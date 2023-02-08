package pl.mbrzozowski.ranger.event;

import org.junit.jupiter.api.Assertions;
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

    @Test
    void checkRequest_AllParamNull_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_NameBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName(" ");
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyNameBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName(" ");
        eventRequest.setDate("1.01.2000");
        eventRequest.setTime("23:59");
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_DateBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setDate(" ");
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyDateBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        eventRequest.setDate(" ");
        eventRequest.setTime("23:59");
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_TimeBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setTime(" ");
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyTimeBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        eventRequest.setDate("1.01.2000");
        eventRequest.setTime(" ");
        Assertions.assertFalse(eventService.checkRequest(eventRequest));
    }

    @Test
    void checkRequest_GoodAllReqParam_ReturnTrue() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        eventRequest.setDate("1.01.2000");
        eventRequest.setTime("23:59");
        Assertions.assertTrue(eventService.checkRequest(eventRequest));
    }
}
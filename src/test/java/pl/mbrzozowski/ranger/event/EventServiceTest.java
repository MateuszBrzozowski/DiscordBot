package pl.mbrzozowski.ranger.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mbrzozowski.ranger.event.reminder.Timers;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.repository.main.EventRepository;

import java.util.ArrayList;
import java.util.List;

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
    void getMainList() {
        Event event = Event.builder().build();
        Player player = Player.builder().event(event).mainList(true).build();
        Player player1 = Player.builder().event(event).mainList(false).build();
        ArrayList<Player> players = new ArrayList<>(List.of(player1, player));
        event.setPlayers(players);
        List<Player> resultMainList = eventService.getMainList(event);
        List<Player> exceptedMainList = new ArrayList<>(List.of(player));
        Assertions.assertEquals(exceptedMainList,resultMainList);
    }

    @Test
    void getMainList_ReturnEmptyList() {
        Event event = Event.builder().build();
        Player player = Player.builder().event(event).mainList(false).build();
        ArrayList<Player> players = new ArrayList<>(List.of(player));
        event.setPlayers(players);
        List<Player> resultMainList = eventService.getMainList(event);
        List<Player> exceptedMainList = new ArrayList<>();
        Assertions.assertEquals(exceptedMainList,resultMainList);
    }

    @Test
    void getReserveList() {
        Event event = Event.builder().build();
        Player player = Player.builder().event(event).mainList(true).build();
        Player player1 = Player.builder().event(event).mainList(false).build();
        ArrayList<Player> players = new ArrayList<>(List.of(player1, player));
        event.setPlayers(players);
        List<Player> resultMainList = eventService.getReserveList(event);
        List<Player> exceptedMainList = new ArrayList<>(List.of(player1));
        Assertions.assertEquals(exceptedMainList,resultMainList);
    }

    @Test
    void getReserveList_ReturnEmptyList() {
        Event event = Event.builder().build();
        Player player = Player.builder().event(event).mainList(true).build();
        ArrayList<Player> players = new ArrayList<>(List.of(player));
        event.setPlayers(players);
        List<Player> resultMainList = eventService.getReserveList(event);
        List<Player> exceptedMainList = new ArrayList<>();
        Assertions.assertEquals(exceptedMainList,resultMainList);
    }
}
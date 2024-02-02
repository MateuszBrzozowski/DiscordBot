package pl.mbrzozowski.ranger.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mbrzozowski.ranger.event.reminder.Timers;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.exceptions.FullListException;
import pl.mbrzozowski.ranger.repository.main.EventRepository;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

class EventServiceTest {

    EventService eventService;

    @BeforeEach
    void beforeEach() {
        UsersReminderService usersReminderService = mock(UsersReminderService.class);
        EventRepository eventRepository = mock(EventRepository.class);
        SettingsService settingsService = mock(SettingsService.class);
        Timers timers = mock(Timers.class);
        eventService = new EventService(usersReminderService, eventRepository, settingsService, timers);
    }

    @Test
    void getMainList() {
        Event event = Event.builder().build();
        Player player = Player.builder().event(event).mainList(true).build();
        Player player1 = Player.builder().event(event).mainList(false).build();
        ArrayList<Player> players = new ArrayList<>(List.of(player1, player));
        event.setPlayers(players);
        List<Player> resultMainList = eventService.getMainList(event);
        ArrayList<Player> exceptedMainList = new ArrayList<>(List.of(player1, player));
        Assertions.assertEquals(exceptedMainList, resultMainList);
    }

    @Test
    void updateEmbed_FullMainList_ThrowFullListException() {
        ArrayList<Player> players = new ArrayList<>();
        Event event = Event.builder().players(players).build();
        Player player = Player.builder().timestamp(LocalDateTime.now()).mainList(true).userName("a").build();
        for (int i = 0; i < 512; i++) { //1024 / 2 - add "/n" (1 char) to each line
            event.getPlayers().add(player);
        }
        Assertions.assertThrows(FullListException.class, () -> eventService.updateEmbed(event));
    }

    @Test
    void updateEmbed_FullReserveList_ThrowFullListException() {
        ArrayList<Player> players = new ArrayList<>();
        Event event = Event.builder().players(players).build();
        Player player = Player.builder().timestamp(LocalDateTime.now()).mainList(false).userName("a").build();
        for (int i = 0; i < 512; i++) { //1024 / 2 - add "/n" (1 char) to each line
            event.getPlayers().add(player);
        }
        Assertions.assertThrows(FullListException.class, () -> eventService.updateEmbed(event));
    }
}
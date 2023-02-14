package pl.mbrzozowski.ranger.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

class EventRequestTest {

    @ParameterizedTest
    @CsvSource(value = {"2023:1:1:0:0:1.1.2023", "2023:12:31:23:59:31.12.2023"}, delimiter = ':')
    void getDate(int year, int month, int day, int hour, int minute, String excepted) {
        EventRequest eventRequest = new EventRequest();
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
        eventRequest.setDateTime(dateTime);
        String result = eventRequest.getDate();
        Assertions.assertEquals(excepted, result);
    }

    @ParameterizedTest
    @CsvSource(value = {"2023.1.1.0.0.00:00", "2023.12.31.23.59.23:59"}, delimiter = '.')
    void getTime(int year, int month, int day, int hour, int minute, String excepted) {
        EventRequest eventRequest = new EventRequest();
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
        eventRequest.setDateTime(dateTime);
        String result = eventRequest.getTime();
        Assertions.assertEquals(excepted, result);
    }


}
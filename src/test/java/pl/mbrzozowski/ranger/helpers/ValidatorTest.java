package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.mbrzozowski.ranger.event.EventRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static pl.mbrzozowski.ranger.helpers.Constants.ZONE_ID_EUROPE_PARIS;

class ValidatorTest {

    @Test
    void checkRequest_AllParamNull_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_NameBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName(" ");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyNameBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName(" ");
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        eventRequest.setDateTime(dateTime);
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyDateNull_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_GoodAllReqParam_ReturnTrue() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        eventRequest.setDateTime(dateTime);
        Assertions.assertTrue(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void isThreeHoursToEvent_ReturnTrue() {
        LocalDateTime eventDateTime = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS))
                .plusHours(3)
                .minusMinutes(1);
        boolean result = Validator.isThreeHoursToEvent(eventDateTime);
        Assertions.assertTrue(result);
    }

    @Test
    void isThreeHoursToEvent_ReturnFalse() {
        LocalDateTime eventDateTime = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS))
                .plusHours(3)
                .plusMinutes(1);
        boolean result = Validator.isThreeHoursToEvent(eventDateTime);
        Assertions.assertFalse(result);
    }

    @Test
    void isEventDateTimeAfterNow_ReturnTrue() {
        LocalDateTime eventDateTime = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS))
                .plusMinutes(1);
        boolean result = Validator.isDateTimeAfterNow(eventDateTime);
        Assertions.assertTrue(result);
    }

    @Test
    void isEventDateTimeAfterNow_ReturnFalse() {
        LocalDateTime eventDateTime = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS))
                .minusMinutes(1);
        boolean result = Validator.isDateTimeAfterNow(eventDateTime);
        Assertions.assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0:00", "0:01", "9:59", "10:01", "23:59"})
    void isTimeFormat_Correct_ReturnTrue(String source) {
        boolean result = Validator.isTimeValid(source);
        Assertions.assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0:0", "9:60", "10:0", "24:59", ":00", "00:"})
    void isTimeFormat_Incorrect_ReturnFalse(String source) {
        boolean result = Validator.isTimeValid(source);
        Assertions.assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1.2023","1.01.2023","01.01.2023","29.02.2024","15.9.2023","31.12.2023"})
    void isDateFormat_Correct_ReturnTrue(String source) {
        boolean result = Validator.isDateValid(source);
        Assertions.assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"32.1.2023","29.02.2023","01.13.2023","15.9.20233","1.2023","2023","1..2023",".1.2023"})
    void isDateFormat_Incorrect_ReturnFalse(String source) {
        boolean result = Validator.isDateValid(source);
        Assertions.assertFalse(result);
    }
}
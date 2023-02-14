package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.mbrzozowski.ranger.event.EventRequest;

import java.time.LocalDateTime;

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

}
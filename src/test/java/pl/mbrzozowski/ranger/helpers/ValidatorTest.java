package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.mbrzozowski.ranger.event.EventRequest;

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
        eventRequest.setDate("1.01.2000");
        eventRequest.setTime("23:59");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_DateBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setDate(" ");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyDateBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        eventRequest.setDate(" ");
        eventRequest.setTime("23:59");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_TimeBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setTime(" ");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_OnlyTimeBlank_ReturnFalse() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        eventRequest.setDate("1.01.2000");
        eventRequest.setTime(" ");
        Assertions.assertFalse(Validator.isValidEventRequest(eventRequest));
    }

    @Test
    void checkRequest_GoodAllReqParam_ReturnTrue() {
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Name");
        eventRequest.setDate("1.01.2000");
        eventRequest.setTime("23:59");
        Assertions.assertTrue(Validator.isValidEventRequest(eventRequest));
    }

}
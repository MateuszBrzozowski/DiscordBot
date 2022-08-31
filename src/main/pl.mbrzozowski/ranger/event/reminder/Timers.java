package ranger.event.reminder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Service
@Slf4j
public class Timers {

    private final List<MyTimer> timers = new ArrayList<>();

    public void add(MyTimer timer) {
        timers.add(timer);
        log.info("Zapamiętuje timer");
    }

    public void cancel(String eventID) {
        log.info(String.valueOf(timers.size()));
        for (MyTimer myTimer : timers) {
            log.info(eventID + "  -  " + myTimer.getEventID());
            if (myTimer.getEventID().equalsIgnoreCase(eventID)) {
                Timer timer = myTimer.getTimer();
                timer.cancel();
                log.info("Anuluje timer.");
            }
        }
        timers.removeIf(myTimer -> myTimer.getEventID().equalsIgnoreCase(eventID));
    }
}

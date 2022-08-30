package ranger.event.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Timers {

    private List<MyTimer> timers = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public void add(MyTimer timer) {
        timers.add(timer);
        logger.info("Zapamiętuje timer");
    }

    public void cancel(String eventID) {
        for (int i = 0; i < timers.size(); i++) {
            if (timers.get(i).getEventID().equalsIgnoreCase(eventID)) {
                Timer timer = timers.get(i).getTimer();
                timer.cancel();
                timers.remove(i);
                logger.info("Anuluje timer.");
            }
        }
    }
}

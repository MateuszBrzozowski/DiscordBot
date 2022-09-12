package pl.mbrzozowski.ranger.event.reminder;

import java.util.Timer;

public class MyTimer {

    private String eventID;
    private Timer timer;

    /**
     * @param eventID ID eventu - ID wiadomości na której znajduję się lista
     * @param timer Timer który ma byćuruchomiony przed eventem
     */
    public MyTimer(String eventID, Timer timer) {
        this.eventID = eventID;
        this.timer = timer;
    }

    public String getEventID() {
        return eventID;
    }

    public Timer getTimer() {
        return timer;
    }
}

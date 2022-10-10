package pl.mbrzozowski.ranger.event.reminder;

import java.util.Timer;

public class MyTimer {

    private final String eventID;
    private final String channelId;
    private final Timer timer;

    /**
     * @param eventID ID eventu - ID wiadomości na której znajduję się lista
     * @param timer   Timer który ma byćuruchomiony przed eventem
     */
    public MyTimer(String eventID, String channelId, Timer timer) {
        this.eventID = eventID;
        this.channelId = channelId;
        this.timer = timer;
    }

    public String getEventID() {
        return eventID;
    }

    public Timer getTimer() {
        return timer;
    }

    public String getChannelId() {
        return channelId;
    }
}

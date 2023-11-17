package pl.mbrzozowski.ranger.event.reminder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Slf4j
@Repository
public class Timers {

    private final List<MyTimer> timers = new ArrayList<>();

    public void add(MyTimer timer) {
        timers.add(timer);
    }

    public void cancelByMsgId(String msgId) {
        for (MyTimer myTimer : timers) {
            if (myTimer.getEventID().equalsIgnoreCase(msgId)) {
                Timer timer = myTimer.getTimer();
                timer.cancel();
                log.info("(msgId={}) - timer canceled", msgId);
            }
        }
        timers.removeIf(myTimer -> myTimer.getEventID().equalsIgnoreCase(msgId));
    }

    public void cancelByChannelId(String channelId) {
        for (MyTimer myTimer : timers) {
            if (myTimer.getChannelId().equalsIgnoreCase(channelId)) {
                Timer timer = myTimer.getTimer();
                timer.cancel();
                log.info("(channelId={}) - timer canceled", channelId);
            }
        }
        timers.removeIf(myTimer -> myTimer.getChannelId().equalsIgnoreCase(channelId));
    }
}

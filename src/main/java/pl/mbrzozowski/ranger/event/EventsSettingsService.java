package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventsSettingsService {

    private final List<EventsSettings> eventsSettings = new ArrayList<>();

    public void addEventsSettings(EventsSettings es) {
        eventsSettings.add(es);
    }

    public int userHaveActiveSettingsPanel(String userID) {
        if (!eventsSettings.isEmpty()) {
            for (int i = 0; i < eventsSettings.size(); i++) {
                if (eventsSettings.get(i).getUserID().equalsIgnoreCase(userID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void saveAnswerAndNextStage(int index, MessageReceivedEvent event) {
        eventsSettings.get(index).saveAnswerAndSetNextStage(event);
    }

    public void removeSettingsPanel(int index) {
        eventsSettings.remove(index);
    }

    public boolean isPossibleEditing(int index) {
        return eventsSettings.get(index).isPossiblyEditing();
    }
}

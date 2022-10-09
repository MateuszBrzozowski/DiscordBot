package pl.mbrzozowski.ranger;

import net.dv8tion.jda.api.JDA;
import pl.mbrzozowski.ranger.event.EventsSettingsModel;

public class Repository {

    private static final EventsSettingsModel eventsSettingsModel = new EventsSettingsModel();
    private static JDA jda;


    public static EventsSettingsModel getEventsSettingsModel() {
        return eventsSettingsModel;
    }

    public static JDA getJda() {
        return jda;
    }

    static void setJDA(JDA j) {
        jda = j;
    }

}

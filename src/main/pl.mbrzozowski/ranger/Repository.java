package ranger;

import net.dv8tion.jda.api.JDA;
import ranger.counter.Counter;
import ranger.event.EventsGeneratorModel;
import ranger.event.EventsSettingsModel;
import ranger.event.reminder.Timers;
import ranger.model.BotWriter;
import ranger.server.service.ServerService;
import ranger.stats.ServerStats;

public class Repository {

    private static final EventsGeneratorModel eventsGeneratorModel = new EventsGeneratorModel();
    private static final EventsSettingsModel eventsSettingsModel = new EventsSettingsModel();
    private static final Timers timers = new Timers();
    private static final BotWriter botWriter = new BotWriter();
    private static final Counter counter = new Counter();
    private static final ServerService serverService = new ServerService();
    private static final ServerStats serverStats = new ServerStats();
    private static JDA jda;


    public static EventsGeneratorModel getEventsGeneratorModel() {
        return eventsGeneratorModel;
    }

    public static EventsSettingsModel getEventsSettingsModel() {
        return eventsSettingsModel;
    }

    public static JDA getJda() {
        return jda;
    }

    static void setJDA(JDA j) {
        jda = j;
    }

    public static Timers getTimers() {
        return timers;
    }

    public static BotWriter getBotWriter() {
        return botWriter;
    }

    public static Counter getCounter() {
        return counter;
    }

    public static ServerService getServerService() {
        return serverService;
    }

    public static ServerStats getServerStats() {
        return serverStats;
    }
}

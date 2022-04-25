package ranger;

import counter.Counter;
import event.Event;
import event.EventsGeneratorModel;
import event.EventsSettingsModel;
import event.reminder.Timers;
import model.BotWriter;
import model.DiceGames;
import net.dv8tion.jda.api.JDA;
import questionnaire.Questionnaires;
import recrut.Recruits;
import server.service.ServerService;
import stats.ServerStats;

public class Repository {

    private static final Event event = new Event();
    private static final Recruits recruits = new Recruits();
    private static final DiceGames diceGames = new DiceGames();
    private static final EventsGeneratorModel eventsGeneratorModel = new EventsGeneratorModel();
    private static final EventsSettingsModel eventsSettingsModel = new EventsSettingsModel();
    private static final Timers timers = new Timers();
    private static final Questionnaires questionnaires = new Questionnaires();
    private static final BotWriter botWriter = new BotWriter();
    private static final Counter counter = new Counter();
    private static final ServerService serverService = new ServerService();
    private static final ServerStats serverStats = new ServerStats();
    private static JDA jda;

    public static Event getEvent() {
        return event;
    }

    public static Recruits getRecruits() {
        return recruits;
    }

    public static DiceGames getDiceGames() {
        return diceGames;
    }

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

    public static Questionnaires getQuestionnaires() {
        return questionnaires;
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

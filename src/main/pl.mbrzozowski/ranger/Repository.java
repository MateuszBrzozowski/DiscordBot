package ranger;

import event.Event;
import event.EventsGeneratorModel;
import event.EventsSettingsModel;
import event.reminder.Timers;
import model.BotWriter;
import model.DiceGames;
import net.dv8tion.jda.api.JDA;
import questionnaire.Questionnaires;
import recrut.Recruits;

public class Repository {

    private static Event event = new Event();
    private static Recruits recruits = new Recruits();
    private static DiceGames diceGames = new DiceGames();
    private static EventsGeneratorModel eventsGeneratorModel = new EventsGeneratorModel();
    private static EventsSettingsModel eventsSettingsModel = new EventsSettingsModel();
    private static Timers timers = new Timers();
    private static Questionnaires questionnaires = new Questionnaires();
    private static BotWriter botWriter = new BotWriter();
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

    public static Questionnaires getQuestionnaires(){
        return questionnaires;
    }
}

package bot.event.writing;

import event.Event;
import model.BotWriter;
import model.DiceGames;
import ranger.Repository;
import recrut.Recruits;

public abstract class Proccess {

    private Recruits recruits = Repository.getRecruits();
    private Event events = Repository.getEvent();
    private DiceGames dice = Repository.getDiceGames();
    private BotWriter botWriter = Repository.getBotWriter();
    private Proccess nextProccess;

    abstract public void proccessMessage(Message message);

    public void setNextProccess(Proccess nexxt) {
        this.nextProccess = nexxt;
    }

    protected Proccess getNextProccess() {
        return nextProccess;
    }

    public Recruits getRecruits() {
        return recruits;
    }

    public Event getEvents() {
        return events;
    }

    public DiceGames getDice() {
        return dice;
    }

    public BotWriter getBotWriter() {
        return botWriter;
    }
}

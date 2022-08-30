package ranger.event.writing;

import ranger.counter.Counter;
import ranger.event.EventService;
import ranger.model.BotWriter;
import ranger.model.DiceGames;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;
import ranger.recrut.Recruits;

public abstract class Proccess {

    private final Recruits recruits = Repository.getRecruits();
    private final EventService events = Repository.getEvent();
    private final DiceGames dice = Repository.getDiceGames();
    private final BotWriter botWriter = Repository.getBotWriter();
    private final Counter counter = Repository.getCounter();
    private Proccess nextProccess;
    protected MessageReceivedEvent messageReceived;


    public Proccess(MessageReceivedEvent messageReceived) {
        this.messageReceived = messageReceived;
    }

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

    public EventService getEvents() {
        return events;
    }

    public DiceGames getDice() {
        return dice;
    }

    public BotWriter getBotWriter() {
        return botWriter;
    }

    public Counter getCounter() {
        return counter;
    }
}

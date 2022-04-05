package bot.event.writing;

import counter.Counter;
import event.Event;
import model.BotWriter;
import model.DiceGames;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import ranger.Repository;
import recrut.Recruits;

public abstract class Proccess {

    private final Recruits recruits = Repository.getRecruits();
    private final Event events = Repository.getEvent();
    private final DiceGames dice = Repository.getDiceGames();
    private final BotWriter botWriter = Repository.getBotWriter();
    private final Counter counter = Repository.getCounter();
    private Proccess nextProccess;
    protected GuildMessageReceivedEvent guildEvent;
    protected PrivateMessageReceivedEvent privateEvent;

    abstract public void proccessMessage(Message message);

    public void setNextProccess(Proccess nexxt) {
        this.nextProccess = nexxt;
    }

    public void setGuildEvent(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    public void setPrivateEvent(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
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

    public Counter getCounter() {
        return counter;
    }
}

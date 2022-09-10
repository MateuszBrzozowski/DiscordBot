package ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;
import ranger.counter.Counter;

public abstract class Proccess {

//    private final BotWriter botWriter = Repository.getBotWriter();
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

//    public BotWriter getBotWriter() {
//        return botWriter;
//    }

    public Counter getCounter() {
        return counter;
    }
}

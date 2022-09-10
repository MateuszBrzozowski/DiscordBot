package ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Proccess {

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
}

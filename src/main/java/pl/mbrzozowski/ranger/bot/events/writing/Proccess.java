package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Proccess {

    private Proccess nextProccess;

    abstract public void proccessMessage(MessageReceivedEvent message);

    public void setNextProccess(Proccess next) {
        this.nextProccess = next;
    }

    protected Proccess getNextProccess() {
        return nextProccess;
    }
}

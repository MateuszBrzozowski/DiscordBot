package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CheckIsPrivateChannel extends Proccess {

    public CheckIsPrivateChannel(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.isPrivate()) {
            getNextProccess().proccessMessage(message);
        }
    }
}

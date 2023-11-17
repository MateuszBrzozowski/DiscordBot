package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class CheckUser extends Proccess {

    public CheckUser(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        if (message.isClanMember()) {
            getNextProccess().proccessMessage(message);
        }
    }
}

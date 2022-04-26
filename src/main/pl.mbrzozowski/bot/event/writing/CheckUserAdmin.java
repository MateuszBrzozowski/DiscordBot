package bot.event.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CheckUserAdmin extends Proccess {

    public CheckUserAdmin(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.isAdmin()) {
            getNextProccess().proccessMessage(message);
        } else {
            if (messageReceived != null) {
                InvalidCmd invalidCmd = new InvalidCmd(messageReceived);
                invalidCmd.proccessMessage(message);
            }
        }
    }
}

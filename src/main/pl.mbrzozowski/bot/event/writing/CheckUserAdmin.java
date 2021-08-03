package bot.event.writing;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class CheckUserAdmin extends Proccess {

    private PrivateMessageReceivedEvent privateEvent;

    public CheckUserAdmin(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.isAdmin()) {
            getNextProccess().proccessMessage(message);
        } else {
            if (privateEvent != null){
                InvalidCmd invalidCmd = new InvalidCmd();
                invalidCmd.proccessMessage(message);
            }
        }
    }
}

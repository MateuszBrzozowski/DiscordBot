package bot.event.writing;

public class CheckUser extends Proccess {

    @Override
    public void proccessMessage(Message message) {
        if (message.isClanMember()) {
            getNextProccess().proccessMessage(message);
        }
    }
}

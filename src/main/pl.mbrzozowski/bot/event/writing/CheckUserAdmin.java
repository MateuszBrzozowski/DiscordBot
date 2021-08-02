package bot.event.writing;

public class CheckUserAdmin extends Proccess {

    @Override
    public void proccessMessage(Message message) {
        if (message.isAdmin()) {
            getNextProccess().proccessMessage(message);
        }
    }
}

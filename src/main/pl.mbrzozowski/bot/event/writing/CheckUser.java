package bot.event.writing;

import helpers.RangerLogger;

public class CheckUser extends Proccess {

    @Override
    public void proccessMessage(Message message) {
        if (message.isClanMember()) {
            getNextProccess().proccessMessage(message);
        } else {
            RangerLogger.info("Brak uprawnień.");
        }
    }
}

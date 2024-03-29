package pl.mbrzozowski.ranger.disboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;

import java.util.TimerTask;

@Slf4j
public class DeleteMessages extends TimerTask {

    private final Message reqMessage;
    private final Message disboardMessage;

    public DeleteMessages(Message reqMessage, Message disboardMessage) {
        this.reqMessage = reqMessage;
        this.disboardMessage = disboardMessage;
    }

    @Override
    public void run() {
        try {
            reqMessage.delete().queue();
            log.info("Deleted reqMessage");
        } catch (Exception e) {
            log.info("It is not possible to delete the reqMessage");
        }
        try {
            disboardMessage.delete().queue();
            log.info("Deleted disboardMessage");
        } catch (Exception e) {
            log.info("It is not possible to delete the disboardMessage");
        }
    }
}

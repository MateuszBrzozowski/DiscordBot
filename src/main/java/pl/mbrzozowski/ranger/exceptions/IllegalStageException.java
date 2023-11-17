package pl.mbrzozowski.ranger.exceptions;

import net.dv8tion.jda.api.entities.User;
import pl.mbrzozowski.ranger.event.EventGeneratorStatus;

public class IllegalStageException extends RuntimeException {

    public IllegalStageException(User user, EventGeneratorStatus stageOfGenerator) {
        super(user + " - Stage(" + stageOfGenerator + ") - Generator stage cannot be selected");
    }
}

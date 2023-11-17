package pl.mbrzozowski.ranger.exceptions;

import net.dv8tion.jda.api.entities.User;
import pl.mbrzozowski.ranger.event.EventGeneratorStatus;
import pl.mbrzozowski.ranger.event.EventSettingsStatus;

public class IllegalStageException extends RuntimeException {

    public IllegalStageException(User user, EventGeneratorStatus stageOfGenerator) {
        super(user + " - Stage(" + stageOfGenerator + ") - Generator stage cannot be selected");
    }

    public IllegalStageException(User user, EventSettingsStatus stageOfSettings) {
        super(user + " - Stage(" + stageOfSettings + ") - Settings stage cannot be selected");
    }
}

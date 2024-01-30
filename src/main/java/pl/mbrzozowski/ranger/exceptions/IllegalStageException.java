package pl.mbrzozowski.ranger.exceptions;

import net.dv8tion.jda.api.entities.User;
import pl.mbrzozowski.ranger.event.EventGeneratorStatus;
import pl.mbrzozowski.ranger.event.EventSettingsStatus;
import pl.mbrzozowski.ranger.giveaway.GiveawayGeneratorStage;

public class IllegalStageException extends RuntimeException {

    public IllegalStageException() {
        super();
    }

    public IllegalStageException(String msg) {
        super(msg);
    }

    public IllegalStageException(User user, EventGeneratorStatus stageOfGenerator) {
        super(user + " - Stage(" + stageOfGenerator + ") - Generator stage cannot be selected");
    }

    public IllegalStageException(User user, EventSettingsStatus stageOfSettings) {
        super(user + " - Stage(" + stageOfSettings + ") - Settings stage cannot be selected");
    }

    public IllegalStageException(GiveawayGeneratorStage stage) {
        super("Stage - " + stage);
    }
}

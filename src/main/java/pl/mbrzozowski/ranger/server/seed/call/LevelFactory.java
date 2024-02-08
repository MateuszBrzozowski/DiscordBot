package pl.mbrzozowski.ranger.server.seed.call;

import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

public class LevelFactory extends Factory {

    @Override
    public MessageCall getLevelOfMessageCall(@NotNull Levels level, SettingsService settingsService) {
        switch (level) {
            case ONE -> {
                return new MessageCall(settingsService, SettingsKey.SEED_CALL_LEVEL_ONE, level);
            }
            case TWO -> {
                return new MessageCall(settingsService, SettingsKey.SEED_CALL_LEVEL_TWO, level);
            }
            case THREE -> {
                return new MessageCall(settingsService, SettingsKey.SEED_CALL_LEVEL_THREE, level);
            }
            case FOUR -> {
                return new MessageCall(settingsService, SettingsKey.SEED_CALL_LEVEL_FOUR, level);
            }
            default -> throw new UnsupportedOperationException(String.valueOf(level));
        }
    }
}

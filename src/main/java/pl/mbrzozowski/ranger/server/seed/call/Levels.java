package pl.mbrzozowski.ranger.server.seed.call;

import org.jetbrains.annotations.NotNull;

public enum Levels {

    ONE(1), //ZMIENIONY z squad
    TWO(2), // zmieniony z live
    THREE(3),
    FOUR(4),
    END(0);

    private final int level;
    private static final Levels[] ENUMS = Levels.values();

    Levels(int level) {
        this.level = level;
    }

    int getLevel() {
        return level;
    }

    @NotNull
    public static Levels getLevel(int level) {
        for (Levels anEnum : ENUMS) {
            if (anEnum.getLevel() == level) {
                return anEnum;
            }
        }
        throw new UnsupportedOperationException(String.valueOf(level));
    }
}

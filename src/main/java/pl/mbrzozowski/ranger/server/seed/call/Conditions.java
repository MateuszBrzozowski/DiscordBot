package pl.mbrzozowski.ranger.server.seed.call;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Conditions(int playersCount, int withinMinutes) {

    public int getPlayersCount() {
        return playersCount;
    }

    public int getWithinMinutes() {
        return withinMinutes;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String toString() {
        return "Conditions{" +
                "playersCount=" + playersCount +
                ", withinMinutes=" + withinMinutes +
                '}';
    }
}

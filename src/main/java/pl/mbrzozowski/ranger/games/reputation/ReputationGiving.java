package pl.mbrzozowski.ranger.games.reputation;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public record ReputationGiving(@NotNull String userId, @NotNull String targetUserId,
                               @NotNull LocalDateTime localDateTime) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReputationGiving that = (ReputationGiving) o;
        return userId.equals(that.userId) && targetUserId.equals(that.targetUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, targetUserId);
    }
}

package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
public class Analyzer {

    public static final int OFFSET = 2;

    public boolean analyzeConditionsWithPlayerCount(List<PlayerCounts> players, List<Conditions> conditions) {
        if (players == null || players.isEmpty() || conditions == null || conditions.isEmpty()) {
            log.debug("Null or empty {}, {}", players, conditions);
            return false;
        }
        log.debug(String.valueOf(conditions.size()));
        for (Conditions condition : conditions) {
            LocalDateTime dateTime = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(condition.getWithinMinutes());
            players.removeIf(playerCounts -> playerCounts.getTime().isBefore(dateTime));
            if (players.size() == 0) {
                log.debug("Players list size after filter dateTime = 0");
                continue;
            }
            int sizeListAfterFilter = players.stream()
                    .filter(playerCounts -> playerCounts.getPlayers() >= condition.getPlayersCount() - OFFSET).toList().size();
            log.debug("Size of list after filter players counts={}, Original size={}", sizeListAfterFilter, players.size());
            if (sizeListAfterFilter == players.size()) {
                log.debug("All record is accepted");
                return true;
            }
            log.debug("Condition not fulfilled, {}", condition);
        }
        return false;
    }

    public boolean analyzeConditions(int players, int minutes) {
        log.debug("Players={}, Minutes={}", players, minutes);
        return players > 0 && players <= 100 && minutes > 0 && minutes <= 120;
    }
}

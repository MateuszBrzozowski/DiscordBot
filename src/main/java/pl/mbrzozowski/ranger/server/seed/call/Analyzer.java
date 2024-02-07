package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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

    public boolean analyzeConditionsWhileStart(List<PlayerCounts> players, List<Conditions> conditions) {
        if (players == null || players.isEmpty() || conditions == null || conditions.isEmpty()) {
            log.debug("Null or empty {}, {}", players, conditions);
            return true;
        }
        int maxPlayersInAnyCondition = Integer.MIN_VALUE;
        for (Conditions condition : conditions) {
            if (condition.getPlayersCount() > maxPlayersInAnyCondition) {
                maxPlayersInAnyCondition = condition.getPlayersCount();
            }
        }
        log.debug("Max Player in conditions: {}", maxPlayersInAnyCondition);
        try {
            players.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        } catch (NullPointerException e) {
            return true;
        }
        int index = getIndexOfFirstEmptyServer(players);
        players = removeEverythingAfterIndex(players, index);
        final int finalMaxPlayersInAnyCondition = maxPlayersInAnyCondition;
        List<PlayerCounts> recordsWithPlayersAboveConditions = new ArrayList<>(players
                .stream()
                .filter(playerCounts -> playerCounts.getPlayers() != null &&
                        playerCounts.getPlayers() >= finalMaxPlayersInAnyCondition).toList());
        recordsWithPlayersAboveConditions.addAll(players.stream().filter(playerCounts -> playerCounts.getPlayers() == null).toList());
        if (recordsWithPlayersAboveConditions.size() > 0) {
            log.debug("PLayers count more than max player from conditions");
            return true;
        }
        return false;
    }

    @NotNull
    private List<PlayerCounts> removeEverythingAfterIndex(@NotNull List<PlayerCounts> players, int index) {
        if (players.size() == index + 1) {
            return players;
        }
        List<PlayerCounts> newPlayers = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            newPlayers.add(players.get(i));
        }
        return newPlayers;
    }

    private int getIndexOfFirstEmptyServer(@NotNull List<PlayerCounts> players) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayers() != null && players.get(i).getPlayers() == 0) {
                return i;
            }
        }
        return players.size() - 1;
    }

    public boolean analyzeConditions(int players, int minutes) {
        log.debug("Players={}, Minutes={}", players, minutes);
        return players > 0 && players <= 100 && minutes > 0 && minutes <= 120;
    }
}

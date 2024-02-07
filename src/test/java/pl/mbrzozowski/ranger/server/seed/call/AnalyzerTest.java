package pl.mbrzozowski.ranger.server.seed.call;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockitoAnnotations;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

class AnalyzerTest {


    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void analyzeConditionsWithPlayerCount_PlayersAboveConditions_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(4), 41 - Analyzer.OFFSET, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_PlayerListNull_ReturnFalse() {
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(null, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_ConditionsListNull_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of());
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, null));
    }

    @Test
    void analyzeConditionsWithPlayerCount_NoPlayers_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of());
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_NoConditions_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(4), 41, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of());
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_NoDataOfPlayersAfterConditions_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(6), 41, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_PlayersCountToLow_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(4), 39 - Analyzer.OFFSET, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_AllPlayersCountToLow_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(4), 40 - Analyzer.OFFSET, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(3), 40 - Analyzer.OFFSET, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 39 - Analyzer.OFFSET, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @Test
    void analyzeConditionsWithPlayerCount_AnyPlayersCountToLow_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(4), 40 - Analyzer.OFFSET, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(3), 39 - Analyzer.OFFSET, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40 - Analyzer.OFFSET, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWithPlayerCount(players, conditions));
    }

    @ParameterizedTest
    @CsvSource({"1,1", "100,120", "100,1", "1,120"})
    void analyzeConditions_ReturnTrue(int players, int minutes) {
        Assertions.assertTrue(new Analyzer().analyzeConditions(players, minutes));
    }

    @ParameterizedTest
    @CsvSource({"0,0", "101,121", "0,1", "1,0", "100,121", "101,120"})
    void analyzeConditions_ReturnFalse(int players, int minutes) {
        Assertions.assertFalse(new Analyzer().analyzeConditions(players, minutes));
    }

    @Test
    void analyzeConditionsWhileStart_NullPlayersNullConditions_ReturnTrue() {
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(null, null));
    }

    @Test
    void analyzeConditionsWhileStart_NullPlayer_ReturnTrue() {
        Conditions conditions = new Conditions(40, 5);
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(null, List.of(conditions)));
    }

    @Test
    void analyzeConditionsWhileStart_NullConditions_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, null, 41, null, null, null, null)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, null));
    }

    @Test
    void analyzeConditionsWhileStart_EmptyPlayers_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>();
        Conditions conditions = new Conditions(40, 5);
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, List.of(conditions)));
    }

    @Test
    void analyzeConditionsWhileStart_EmptyConditions_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 41, null, null, null, null)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, List.of()));
    }

    @Test
    void analyzeConditionsWhileStart_NullPlayerValue_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), null, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_NullDateValue_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, null, 39, null, null, null, null),
                new PlayerCounts(1, null, 39, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_ConditionsApproved_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 41, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_ConditionsApprovedByAnyRecordFirst_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 41, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_ConditionsApprovedByAnyRecordMid_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 41, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_ConditionsApprovedByAnyRecordLast_ReturnTrue() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 40, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 41, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertTrue(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_PlayersBelowConditions_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 39, null, null, null, null)));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_AllPlayersBelowConditionsFirst_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 39, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 38, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 38, null, null, null, null)
        ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_AllPlayersBelowConditionsMid_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 30, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 39, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 38, null, null, null, null)
                ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }

    @Test
    void analyzeConditionsWhileStart_AllPlayersBelowConditionsLast_ReturnFalse() {
        List<PlayerCounts> players = new ArrayList<>(List.of(
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 10, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 20, null, null, null, null),
                new PlayerCounts(1, LocalDateTime.now(ZoneOffset.UTC), 39, null, null, null, null)
                ));
        List<Conditions> conditions = new ArrayList<>(List.of(new Conditions(40, 5)));
        Assertions.assertFalse(new Analyzer().analyzeConditionsWhileStart(players, conditions));
    }
}
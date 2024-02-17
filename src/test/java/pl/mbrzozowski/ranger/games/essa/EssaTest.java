package pl.mbrzozowski.ranger.games.essa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class EssaTest {

    @ParameterizedTest
    @CsvSource(value = {"0:0", "100:100"}, delimiter = ':')
    void roundTo5(int value, int excepted) {
        int result = Essa.getInstance().roundTo5(value);
        Assertions.assertEquals(excepted, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void roundTo5_ThrowException(int value) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Essa.getInstance().roundTo5(value));
    }
}
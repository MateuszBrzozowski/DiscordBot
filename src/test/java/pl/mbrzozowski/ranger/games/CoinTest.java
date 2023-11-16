package pl.mbrzozowski.ranger.games;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoinTest {

    @ParameterizedTest
    @CsvSource(value = {"0:Orzeł", "1:Reszka"}, delimiter = ':')
    void convertNumToString(int value, String excepted) {
        String result = Coin.convertNumToString(value);
        Assertions.assertEquals(excepted, result);
    }

    @ParameterizedTest
    @CsvSource(value = {"-1", "2"})
    void convertNumToString_ThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Coin.convertNumToString(-1));
    }
}
package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MathTest {

    @ParameterizedTest
    @CsvSource(value = {"0:0",
            "5:5",
            "11:10",
            "12:10",
            "13:15",
            "14:15",
            "15:15",
            "-5:-5",
            "-11:-10",
            "-12:-10",
            "-13:-15",
            "-14:-15",
            "-15:-15"}, delimiter = ':')
    void roundTo5(int value, int excepted) {
        int result = Math.roundTo5(value);
        Assertions.assertEquals(excepted, result);
    }
}
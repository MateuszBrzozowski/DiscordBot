package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

class ConverterTest {

    @ParameterizedTest
    @CsvSource(value = {"2023,1,1,0,0,1.01.2023 0:00", "2023,12,31,23,59,31.12.2023 23:59", "2024,2,29,23,59,29.02.2024 23:59"}, delimiter = ',')
    void stringToLocalDateTime_Correct_ReturnDateTime(int year, int month, int day, int hour, int minute, String source) {
        LocalDateTime exceptedDateTime = LocalDateTime.of(year, month, day, hour, minute);
        LocalDateTime resultDateTime = Converter.stringToLocalDateTime(source);
        Assertions.assertEquals(exceptedDateTime, resultDateTime);
    }

    @ParameterizedTest
    @ValueSource(strings = {"32.01.2023 0:00",
            "31.13.2023 23:59",
            "31.12.20233 23:59",
            "31.13.2023 24:00",
            "31.13.2023 23:60",
            "29.02.2023 23:59",
            "1.1.202323:59",
            "exampleText"})
    void stringToLocalDateTime_Incorrect_ReturnNull(String source) {
        LocalDateTime resultDateTime = Converter.stringToLocalDateTime(source);
        Assertions.assertNull(resultDateTime);
    }

    @Test
    void stringToLocalDateTime_NullSource_ThrowException() {
        Assertions.assertThrows(NullPointerException.class, () -> Converter.stringToLocalDateTime(null));
    }

    @Test
    void stringToLocalDateTime_EmptySource_ThrowException() {
        String source = "";
        Assertions.assertThrows(NullPointerException.class, () -> Converter.stringToLocalDateTime(source));
    }

    @Test
    void stringToLocalDateTime_BlankSource_ThrowException() {
        String source = " ";
        Assertions.assertThrows(NullPointerException.class, () -> Converter.stringToLocalDateTime(source));
    }
}
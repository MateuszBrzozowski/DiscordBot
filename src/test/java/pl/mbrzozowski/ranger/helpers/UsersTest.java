package pl.mbrzozowski.ranger.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UsersTest {

    @ParameterizedTest
    @CsvSource(value = {"name,name", "<n,n", "n>,n", "\\n,n", "/n,n", ":n,n", "n*,n", "n??,n", "\"n\",n", "|n|n|,nn"})
    void replaceAllIllegalCharsInName(String name, String expected) {
        Assertions.assertEquals(expected, Users.replaceAllIllegalCharsInName(name));
    }
}
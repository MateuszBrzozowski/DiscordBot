package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.utils.TimeFormat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

class EventServiceTest {

    @Test
    void main() {
        //<t:1675015200:F>
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 29, 19, 0);
        long epochSecond = dateTime.atZone(ZoneId.of("Europe/Paris")).toEpochSecond() * 1000;
        System.out.println("1675015200");
        System.out.println(epochSecond);

        System.out.println(TimeFormat.DATE_TIME_LONG.format(epochSecond));
    }
}
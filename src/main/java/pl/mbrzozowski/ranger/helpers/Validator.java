package pl.mbrzozowski.ranger.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventRequest;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static pl.mbrzozowski.ranger.helpers.Constants.ZONE_ID_EUROPE_PARIS;

@Slf4j
public class Validator {

    public static boolean isDateValid(@NotNull String source) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        try {
            LocalDateTime dateTime = LocalDateTime.parse(source + " 23:59", dateTimeFormatter);
            return isDateValidFebruaryOnLeapYear(source.split("\\."), dateTime);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isDateValidFebruaryOnLeapYear(@NotNull String[] splitSource, @NotNull LocalDateTime dateTime) {
        boolean leap = Year.isLeap(dateTime.getYear());
        int day = Integer.parseInt(splitSource[0]);
        return leap || dateTime.getMonth() != Month.FEBRUARY || day < 29;
    }

    public static boolean isTimeValid(@NotNull String time) {
        time = timeCorrect(time);
        if (time.length() == 5) {
            if (time.substring(2, 3).equalsIgnoreCase(":")) {
                if (isDecimal(time.substring(0, 1)) && isDecimal(time.substring(1, 2)) && isDecimal(time.substring(3, 4)) && isDecimal(time.substring(4, 5))) {
                    int hour = Integer.parseInt(time.substring(0, 2));
                    int min = Integer.parseInt(time.substring(3, 5));
                    return hour >= 0 && hour <= 23 && min >= 0 && min <= 59;
                }
            }
        }
        return false;
    }

    @Contract(pure = true)
    private static @NotNull String timeCorrect(@NotNull String time) {
        if (time.length() == 4) {
            time = "0" + time;
        }
        return time;
    }

    private static boolean isDecimal(String s) {
        for (int i = 0; i < 10; i++) {
            if (s.equals(String.valueOf(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEventDateTimeAfterNow(LocalDateTime eventDateTime) {
        if (eventDateTime == null) {
            return false;
        }
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS));
        return eventDateTime.isAfter(dateTimeNow);
    }

    public static boolean isThreeHoursToEvent(LocalDateTime eventDateTime) {
        eventDateTime = eventDateTime.minusHours(3);
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS));
        return dateTimeNow.isAfter(eventDateTime);
    }

    public static boolean isValidEventRequest(@NotNull EventRequest eventRequest) {
        if (StringUtils.isBlank(eventRequest.getName())) {
            return false;
        }
        return eventRequest.getDateTime() != null;
    }
}

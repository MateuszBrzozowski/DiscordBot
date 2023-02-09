package pl.mbrzozowski.ranger.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static pl.mbrzozowski.ranger.helpers.Constants.ZONE_ID_EUROPE_PARIS;

@Slf4j
public class Validator {

    private static final String datePattern = "dd.MM.yyyy";

    public static boolean isDateFormat(String source) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        try {
            LocalDateTime date = LocalDateTime.parse(source + " 23:59", dateTimeFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * @param s Czas w formacie HH:mm lub H:mm
     * @return Zwraca true jeżeli czas jes podany w poprawnej formie, w innym przypadku zwraca false.
     */
    public static boolean isTimeFormat(@NotNull String s) {
        if (s.length() == 5) {
            if (s.substring(2, 3).equalsIgnoreCase(":")) {
                if (isDecimal(s.substring(0, 1)) && isDecimal(s.substring(1, 2)) && isDecimal(s.substring(3, 4)) && isDecimal(s.substring(4, 5))) {
                    int hour = Integer.parseInt(s.substring(0, 2));
                    int min = Integer.parseInt(s.substring(3, 5));
                    if (hour >= 0 && hour <= 23 && min >= 0 && min <= 59) {
                        return true;
                    } else log.error("Time is not valid - Out of bounds");
                } else log.error("Time is not valid - No Decimal");
            } else log.error("Time is not valid :");
        }
        return false;
    }

    @Contract(pure = true)
    public static @NotNull String timeCorrect(@NotNull String timeOld) {
        if (timeOld.length() == 4) {
            timeOld = "0" + timeOld;
        }
        return timeOld;
    }

    private static boolean isDecimal(String s) {
        for (int i = 0; i < 10; i++) {
            if (s.equals(String.valueOf(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza czy data i czas jest później niż obecna data i czas.
     *
     * @param dateTime Data i czas w formacie dd.MM.yyyy HH:mm
     * @return Zwraca true jeżeli podany czas w parametrze jest później niż obecny. W innym przypadku zwraca false.
     */
    public static boolean isEventDateTimeAfterNow(String dateTime) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        LocalDateTime eventDateTime = null;
        try {
            eventDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            log.error("Date time is not valid [" + dateTime + "]");
            return false;
        }
        return eventDateTime.isAfter(dateTimeNow);
    }

    public static boolean eventDateAfterNow(@NotNull LocalDateTime dateTime) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS));
        return dateTime.isAfter(dateTimeNow);
    }

    public static boolean isValidEventRequest(@NotNull EventRequest eventRequest) {
        if (StringUtils.isBlank(eventRequest.getName())) {
            return false;
        }
        if (StringUtils.isBlank(eventRequest.getDate())) {
            return false;
        }
        return !StringUtils.isBlank(eventRequest.getTime());
    }
}

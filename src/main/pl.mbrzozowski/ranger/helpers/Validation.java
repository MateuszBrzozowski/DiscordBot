package ranger.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class Validation {

    private static RangerLogger rangerLogger = new RangerLogger();
    protected static final Logger logger = LoggerFactory.getLogger(Validation.class.getName());
    private static final String datePattern = "dd.MM.yyyy";

    public static boolean isDateFormat(String s) {
        SimpleDateFormat df = new SimpleDateFormat(datePattern);
        df.setLenient(false);
        try {
            Date javaDate = df.parse(s);
            df.parse(s);
        } catch (ParseException e) {
            rangerLogger.info(String.format("Nieprawidłowa data %s. Format daty: \"%s\"", s, datePattern));
            return false;
        }
        return true;
    }

    /**
     * @param s Czas w formacie HH:mm lub H:mm
     * @return Zwraca true jeżeli czas jes podany w poprawnej formie, w innym przypadku zwraca false.
     */
    public static boolean isTimeFormat(String s) {
//        s = timeCorrect(s);
        if (s.length() == 5) {
            if (s.substring(2, 3).equalsIgnoreCase(":")) {
                if (isDecimal(s.substring(0, 1)) && isDecimal(s.substring(1, 2)) && isDecimal(s.substring(3, 4)) && isDecimal(s.substring(4, 5))) {
                    int hour = Integer.parseInt(s.substring(0, 2));
                    int min = Integer.parseInt(s.substring(3, 5));
                    if (hour >= 0 && hour <= 23 && min >= 0 && min <= 59) {
                        return true;
                    } else logger.info("Zly format - godzina lub czas wyszły za zakres");
                } else logger.info("Zly format - to nie jest liczba");
            } else logger.info("Zly format :");
        }
        return false;
    }

    public static String timeCorrect(String timeOld){
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
    public static boolean eventDateTimeAfterNow(String dateTime) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
        LocalDateTime evnetDateTime = null;
        try {
            evnetDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            RangerLogger.info("Nieprawidłowa data i czas [" + dateTime + "]");
            return false;
        }
        if (evnetDateTime.isAfter(dateTimeNow))
            return true;

        return false;
    }
}

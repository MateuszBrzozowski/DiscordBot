package pl.mbrzozowski.ranger.helpers;

import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Converter {

    /**
     * Converting String to {@link LocalDateTime}
     *
     * @param source date in format d.M.yyyy HH:mm
     * @return {@link LocalDateTime} or null if can not to parse string to LocalDateTime
     */
    public static @Nullable LocalDateTime stringToLocalDateTime(String source) {
        if (source == null || source.isBlank()) {
            throw new NullPointerException("Wymagane dane wejściowe");
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        try {
            return LocalDateTime.parse(source, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @NotNull
    public static String LocalDateTimeToTimestampDateTimeLongFormat(@NotNull LocalDateTime source) {
        long epochSecond = source.atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toEpochSecond() * 1000;
        return TimeFormat.DATE_TIME_LONG.format(epochSecond);
    }

    @NotNull
    public static String LocalDateTimeToTimestampRelativeFormat(@NotNull LocalDateTime source) {
        long epochSecond = source.atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toEpochSecond() * 1000;
        return TimeFormat.RELATIVE.format(epochSecond);
    }
}

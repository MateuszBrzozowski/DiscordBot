package pl.mbrzozowski.ranger.helpers;

import net.dv8tion.jda.api.utils.TimeFormat;
import org.apache.commons.lang3.StringUtils;
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
     * @param source date in format d.M.yyyy H:mm
     * @return {@link LocalDateTime} or null if can not to parse string to LocalDateTime
     */
    public static @Nullable LocalDateTime stringToLocalDateTime(String source) {
        if (StringUtils.isBlank(source)) {
            throw new NullPointerException("Wymagane dane wejściowe");
        }
        if (!Validator.isDateValid(source.split(" ")[0])) {
            return null;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy H:mm");
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
    public static String LocalDateTimeToLongDateWWithShortTime(@NotNull LocalDateTime source) {
        long epochSecond = source.atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toEpochSecond() * 1000;
        return TimeFormat.DATE_TIME_SHORT.format(epochSecond);
    }

    @NotNull
    public static String LocalDateTimeToTimestampRelativeFormat(@NotNull LocalDateTime source) {
        long epochSecond = source.atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toEpochSecond() * 1000;
        return TimeFormat.RELATIVE.format(epochSecond);
    }
}

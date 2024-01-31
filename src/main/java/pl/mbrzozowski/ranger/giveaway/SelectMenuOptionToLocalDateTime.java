package pl.mbrzozowski.ranger.giveaway;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class SelectMenuOptionToLocalDateTime {

    @NotNull
    public static LocalDateTime addTime(@NotNull SelectMenuOptionTime option, LocalDateTime startTime) {
        String optionName = option.toString();
        if (optionName.startsWith("MINUTE_")) {
            optionName = optionName.substring("MINUTE_".length());
            return startTime.plusMinutes(Long.parseLong(optionName));
        } else if (optionName.startsWith("HOUR_")) {
            optionName = optionName.substring("HOUR_".length());
            return startTime.plusHours(Long.parseLong(optionName));
        } else {
            throw new IllegalArgumentException(optionName);
        }
    }

    public static LocalDateTime getDate(@NotNull SelectMenuOptionTime exactDate) {
        return exactDate.getDateTime();
    }

    @NotNull
    public static LocalDateTime setTime(@NotNull SelectMenuOptionTime option, LocalDateTime endTime) {
        switch (option) {
            case TIME_8, TIME_9, TIME_10, TIME_11, TIME_12, TIME_13, TIME_14, TIME_15, TIME_16, TIME_17,
                    TIME_18, TIME_19, TIME_20, TIME_21, TIME_22, TIME_23 -> {
                return endTime.withHour(option.getDateTime().getHour()).withMinute(0).withSecond(0);
            }
            default -> throw new IllegalArgumentException(option.name());
        }
    }

}

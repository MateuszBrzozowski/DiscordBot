package pl.mbrzozowski.ranger.giveaway;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class SelectMenuOptionToLocalDateTime {

    public static LocalDateTime addTime(@NotNull SelectMenuOptionTime option, LocalDateTime startTime) {
        switch (option) {
            case MINUTE_1 -> {
                return startTime.plusMinutes(1);
            }
            case MINUTE_2 -> {
                return startTime.plusMinutes(2);
            }
            case MINUTE_3 -> {
                return startTime.plusMinutes(3);
            }
            case MINUTE_4 -> {
                return startTime.plusMinutes(4);
            }
            case MINUTE_5 -> {
                return startTime.plusMinutes(5);
            }
            case MINUTE_6 -> {
                return startTime.plusMinutes(6);
            }
            case MINUTE_7 -> {
                return startTime.plusMinutes(7);
            }
            case MINUTE_8 -> {
                return startTime.plusMinutes(8);
            }
            case MINUTE_9 -> {
                return startTime.plusMinutes(9);
            }
            case MINUTE_10 -> {
                return startTime.plusMinutes(10);
            }
            case MINUTE_15 -> {
                return startTime.plusMinutes(15);
            }
            case MINUTE_20 -> {
                return startTime.plusMinutes(20);
            }
            case MINUTE_25 -> {
                return startTime.plusMinutes(25);
            }
            case MINUTE_30 -> {
                return startTime.plusMinutes(30);
            }
            case MINUTE_35 -> {
                return startTime.plusMinutes(35);
            }
            case MINUTE_40 -> {
                return startTime.plusMinutes(40);
            }
            case MINUTE_45 -> {
                return startTime.plusMinutes(45);
            }
            case MINUTE_50 -> {
                return startTime.plusMinutes(50);
            }
            case MINUTE_55 -> {
                return startTime.plusMinutes(55);
            }
            case HOUR_1 -> {
                return startTime.plusHours(1);
            }
            case HOUR_2 -> {
                return startTime.plusHours(2);
            }
            case HOUR_3 -> {
                return startTime.plusHours(3);
            }
            default -> throw new IllegalArgumentException(option.name() + "(" + option + ")");
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
                return endTime.withHour(option.getDateTime().getHour()).withMinute(0);
            }
            default -> throw new IllegalArgumentException(option.name());
        }
    }

}

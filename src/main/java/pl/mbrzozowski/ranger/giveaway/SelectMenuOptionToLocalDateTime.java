package pl.mbrzozowski.ranger.giveaway;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class SelectMenuOptionToLocalDateTime {

    public static LocalDateTime addTime(@NotNull SelectMenuOption option, LocalDateTime startTime) {
        switch (option) {
            case MINUTES_10 -> {
                return startTime.plusMinutes(10);
            }
            case MINUTES_15 -> {
                return startTime.plusMinutes(15);
            }
            case MINUTES_30 -> {
                return startTime.plusMinutes(30);
            }
            case MINUTES_45 -> {
                return startTime.plusMinutes(45);
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
            case HOUR_4 -> {
                return startTime.plusHours(4);
            }
            case HOUR_5 -> {
                return startTime.plusHours(5);
            }
            case HOUR_6 -> {
                return startTime.plusHours(6);
            }
            case HOUR_8 -> {
                return startTime.plusHours(8);
            }
            case HOUR_10 -> {
                return startTime.plusHours(10);
            }
            case HOUR_12 -> {
                return startTime.plusHours(12);
            }
            case DAY_1 -> {
                return startTime.plusDays(1);
            }
            case DAY_2 -> {
                return startTime.plusDays(2);
            }
            case DAY_3 -> {
                return startTime.plusDays(3);
            }
            case DAY_4 -> {
                return startTime.plusDays(4);
            }
            case DAY_5 -> {
                return startTime.plusDays(5);
            }
            case DAY_6 -> {
                return startTime.plusDays(6);
            }
            case DAY_7 -> {
                return startTime.plusDays(7);
            }
            case DAY_14 -> {
                return startTime.plusDays(14);
            }
            case DAY_21 -> {
                return startTime.plusDays(21);
            }
            default -> throw new IllegalArgumentException(option.name() + "(" + option + ")");
        }

    }

    public static LocalDateTime getDate(@NotNull SelectMenuOption exactDate) {
        return exactDate.getDateTime();
    }

    @NotNull
    public static LocalDateTime setTime(@NotNull SelectMenuOption option, LocalDateTime endTime) {
        switch (option) {
            case TIME_8, TIME_9, TIME_10, TIME_11, TIME_12, TIME_13, TIME_14, TIME_15, TIME_16, TIME_17,
                    TIME_18, TIME_19, TIME_20, TIME_21, TIME_22, TIME_23 -> {
                return endTime.withHour(option.getDateTime().getHour()).withMinute(0);
            }
            default -> throw new IllegalArgumentException(option.name() + "(" + option + ")");
        }
    }

}

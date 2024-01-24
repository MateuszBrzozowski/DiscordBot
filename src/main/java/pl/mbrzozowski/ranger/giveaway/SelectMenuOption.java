package pl.mbrzozowski.ranger.giveaway;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

enum SelectMenuOption {

    DATE_TIME("Określę dokładną date i godzinę", "datetime"),
    TIME_DURATION("Określę czas trwania", "timeDuration"),
    MINUTES_10("10m", "10m"), //2
    MINUTES_15("15m", "15m"),
    MINUTES_30("30m", "30m"),
    MINUTES_45("45m", "45m"),
    HOUR_1("1h", "1h"),
    HOUR_2("2h", "2h"),
    HOUR_3("3h", "3h"),
    HOUR_4("4h", "4h"),
    HOUR_5("5h", "5h"),//10
    HOUR_6("6h", "6h"),
    HOUR_8("8h", "8h"),
    HOUR_10("10h", "10h"),
    HOUR_12("12h", "12h"),
    DAY_1("1d", "1d"),
    DAY_2("2d", "2d"),
    DAY_3("3d", "3d"),
    DAY_4("4d", "4d"),
    DAY_5("5d", "5d"),
    DAY_6("6d", "6d"),//20
    DAY_7("7d", "7d"),
    DAY_14("14d", "14d"),
    DAY_21("21d", "21d"),
    DATE_TODAY(LocalDateTime.now()),//24
    DATE_PLUS_1(LocalDateTime.now().plusDays(1)),
    DATE_PLUS_2(LocalDateTime.now().plusDays(2)),
    DATE_PLUS_3(LocalDateTime.now().plusDays(3)),
    DATE_PLUS_4(LocalDateTime.now().plusDays(4)),
    DATE_PLUS_5(LocalDateTime.now().plusDays(5)),
    DATE_PLUS_6(LocalDateTime.now().plusDays(6)),
    DATE_PLUS_7(LocalDateTime.now().plusDays(7)),
    DATE_PLUS_8(LocalDateTime.now().plusDays(8)),
    DATE_PLUS_9(LocalDateTime.now().plusDays(9)),
    DATE_PLUS_10(LocalDateTime.now().plusDays(10)),
    DATE_PLUS_11(LocalDateTime.now().plusDays(11)),
    DATE_PLUS_12(LocalDateTime.now().plusDays(12)),
    DATE_PLUS_13(LocalDateTime.now().plusDays(13)),
    DATE_PLUS_14(LocalDateTime.now().plusDays(14)),
    DATE_PLUS_15(LocalDateTime.now().plusDays(15)),
    DATE_PLUS_16(LocalDateTime.now().plusDays(16)),
    DATE_PLUS_17(LocalDateTime.now().plusDays(17)),
    DATE_PLUS_18(LocalDateTime.now().plusDays(18)),
    DATE_PLUS_19(LocalDateTime.now().plusDays(19)),
    DATE_PLUS_20(LocalDateTime.now().plusDays(20)),
    DATE_PLUS_21(LocalDateTime.now().plusDays(21)),
    DATE_PLUS_22(LocalDateTime.now().plusDays(22)),
    DATE_PLUS_23(LocalDateTime.now().plusDays(23)),
    DATE_PLUS_24(LocalDateTime.now().plusDays(24)), //48
    TIME_8(LocalDateTime.now().withHour(8).withMinute(0)), //49
    TIME_9(LocalDateTime.now().withHour(9).withMinute(0)),
    TIME_10(LocalDateTime.now().withHour(10).withMinute(0)),
    TIME_11(LocalDateTime.now().withHour(11).withMinute(0)),
    TIME_12(LocalDateTime.now().withHour(12).withMinute(0)),
    TIME_13(LocalDateTime.now().withHour(13).withMinute(0)),
    TIME_14(LocalDateTime.now().withHour(14).withMinute(0)),
    TIME_15(LocalDateTime.now().withHour(15).withMinute(0)),
    TIME_16(LocalDateTime.now().withHour(16).withMinute(0)),
    TIME_17(LocalDateTime.now().withHour(17).withMinute(0)),
    TIME_18(LocalDateTime.now().withHour(18).withMinute(0)),
    TIME_19(LocalDateTime.now().withHour(19).withMinute(0)),
    TIME_20(LocalDateTime.now().withHour(20).withMinute(0)),
    TIME_21(LocalDateTime.now().withHour(21).withMinute(0)),
    TIME_22(LocalDateTime.now().withHour(22).withMinute(0)),
    TIME_23(LocalDateTime.now().withHour(23).withMinute(0)),
    ADD_PRIZE("Dodaj nagrodę", "addPrize"),
    REMOVE_PRIZE("Usuń nagrodę", "removePrize");

    private final String label;
    private final String value;
    private final LocalDateTime dateTime;
    private static final SelectMenuOption[] ENUMS = SelectMenuOption.values();

    SelectMenuOption(String label, String value) {
        this.label = label;
        this.value = value;
        this.dateTime = LocalDateTime.now();
    }

    SelectMenuOption(@NotNull LocalDateTime dateTime) {
        this.label = dateTime.getDayOfMonth() + "." + String.format("%02d", dateTime.getMonthValue()) + "." + dateTime.getYear();
        this.value = dateTime.getDayOfYear() + "H" + dateTime.getHour();
        this.dateTime = dateTime;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Nullable
    public static LocalDateTime getDate(String value) {
        for (SelectMenuOption option : ENUMS) {
            if (option.value.equalsIgnoreCase(value)) {
                return option.dateTime;
            }
        }
        return null;
    }

    @NotNull
    public static Collection<? extends SelectOption> getDurationTimes() {
        List<SelectOption> selectOptions = new ArrayList<>();
        for (int i = 2; i <= 23; i++) {
            SelectOption of = SelectOption.of(ENUMS[i].getLabel(), ENUMS[i].getValue());
            selectOptions.add(of);
        }
        return selectOptions;
    }

    @NotNull
    public static Collection<? extends SelectOption> getDays() {
        List<SelectOption> selectOptions = new ArrayList<>();
        for (int i = 24; i <= 48; i++) {
            SelectOption of = SelectOption.of(ENUMS[i].getLabel(), ENUMS[i].getValue());
            selectOptions.add(of);
        }
        return selectOptions;
    }

    @NotNull
    public static Collection<? extends SelectOption> getHours() {
        List<SelectOption> selectOptions = new ArrayList<>();
        for (int i = 49; i <= 64; i++) {
            String hour = String.valueOf(ENUMS[i].getDateTime().getHour());
            String minute = String.format("%02d", ENUMS[i].getDateTime().getMinute());
            String label = hour + ":" + minute;
            SelectOption of = SelectOption.of(label, ENUMS[i].getValue());
            selectOptions.add(of);
        }
        return selectOptions;
    }

    @NotNull
    public static Collection<? extends SelectOption> getTimeMode() {
        return new ArrayList<>
                (List.of(SelectOption.of(DATE_TIME.getLabel(), DATE_TIME.getValue()),
                        SelectOption.of(TIME_DURATION.getLabel(), TIME_DURATION.getValue())));

    }

    @NotNull
    public static Collection<? extends SelectOption> getPrize() {
        return new ArrayList<>(
                List.of(SelectOption.of(ADD_PRIZE.getLabel(), ADD_PRIZE.getValue()),
                        SelectOption.of(REMOVE_PRIZE.getLabel(), REMOVE_PRIZE.getValue()))
        );
    }
}

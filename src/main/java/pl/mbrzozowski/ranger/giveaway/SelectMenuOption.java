package pl.mbrzozowski.ranger.giveaway;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

enum SelectMenuOption {

    DATE_TIME("Określę dokładną date i godzinę", "datetime"),
    TIME_DURATION("Określę czas trwania", "timeDuration"),
    MINUTE_1("1 minute", "1"), //2
    MINUTE_2("2 minuty", "2"),
    MINUTE_3("3 minuty", "3"),
    MINUTE_4("4 minuty", "4"),
    MINUTE_5("5 minut", "5"),
    MINUTE_6("6 minut", "6"),
    MINUTE_7("7 minut", "7"),
    MINUTE_8("8 minut", "8"),
    MINUTE_9("9 minut", "9"),
    MINUTE_10("10 minut", "10"),
    MINUTE_15("15 minut", "11"),
    MINUTE_20("20 minut", "12"),
    MINUTE_25("25 minut", "13"),
    MINUTE_30("30 minut", "14"),
    MINUTE_35("35 minut", "15"),
    MINUTE_40("40 minut", "16"),
    MINUTE_45("45 minut", "17"),
    MINUTE_50("50 minut", "18"),
    MINUTE_55("55 minut", "19"),
    HOUR_1("1 godzinę", "20"),
    HOUR_2("2 godziny", "21"),
    HOUR_3("3 godziny", "22"),
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
    TIME_8(LocalDateTime.now().withHour(8).withMinute(0).withSecond(0)), //49
    TIME_9(LocalDateTime.now().withHour(9).withMinute(0).withSecond(0)),
    TIME_10(LocalDateTime.now().withHour(10).withMinute(0).withSecond(0)),
    TIME_11(LocalDateTime.now().withHour(11).withMinute(0).withSecond(0)),
    TIME_12(LocalDateTime.now().withHour(12).withMinute(0).withSecond(0)),
    TIME_13(LocalDateTime.now().withHour(13).withMinute(0).withSecond(0)),
    TIME_14(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0)),
    TIME_15(LocalDateTime.now().withHour(15).withMinute(0).withSecond(0)),
    TIME_16(LocalDateTime.now().withHour(16).withMinute(0).withSecond(0)),
    TIME_17(LocalDateTime.now().withHour(17).withMinute(0).withSecond(0)),
    TIME_18(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)),
    TIME_19(LocalDateTime.now().withHour(19).withMinute(0).withSecond(0)),
    TIME_20(LocalDateTime.now().withHour(20).withMinute(0).withSecond(0)),
    TIME_21(LocalDateTime.now().withHour(21).withMinute(0).withSecond(0)),
    TIME_22(LocalDateTime.now().withHour(22).withMinute(0).withSecond(0)),
    TIME_23(LocalDateTime.now().withHour(23).withMinute(0).withSecond(0)),
    ADD_PRIZE("Dodaj nagrodę", "addPrize"),
    REMOVE_PRIZE("Usuń nagrodę", "removePrize"),
    EXCLUDE_YES("Tak", "excludeYes"),
    EXCLUDE_NO("Nie", "excludeNo");

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
        this.value = dateTime.getDayOfYear() + "H" + dateTime.hashCode();
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

    @NotNull
    public static SelectMenuOption getByValue(String value) {
        for (SelectMenuOption option : ENUMS) {
            if (option.value.equalsIgnoreCase(value)) {
                return option;
            }
        }
        throw new NoSuchFieldError("Select menu option not exist (value=" + value + ")");
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

    @NotNull
    public static Collection<? extends SelectOption> getExclude() {
        return new ArrayList<>(
                List.of(SelectOption.of(EXCLUDE_YES.getLabel(), EXCLUDE_YES.getValue()),
                        SelectOption.of(EXCLUDE_NO.getLabel(), EXCLUDE_NO.getValue()))
        );
    }
}

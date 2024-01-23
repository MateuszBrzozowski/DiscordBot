package pl.mbrzozowski.ranger.giveaway;

public enum SelectMenuOption {

    DATE_TIME("Określę dokładną date i godzinę", "datetime"),
    TIME_DURATION("Określę czas trwania", "timeDuration"),
    MINUTES_10("10m", "10m"),
    MINUTES_15("15m", "15m"),
    MINUTES_30("30m", "30m"),
    MINUTES_45("45m", "45m"),
    HOUR_1("1h", "1h"),
    HOUR_2("2h", "2h"),
    HOUR_3("3h", "3h"),
    HOUR_4("4h", "4h"),
    HOUR_5("5h", "5h"),
    HOUR_6("6h", "6h"),
    HOUR_8("8h", "8h"),
    HOUR_10("10h", "10h"),
    HOUR_12("12h", "12h"),
    DAY_1("1d", "1d"),
    DAY_2("2d", "2d"),
    DAY_3("3d", "3d"),
    DAY_4("4d", "4d"),
    DAY_5("5d", "5d"),
    DAY_6("6d", "6d"),
    DAY_7("7d", "7d"),
    DAY_14("14d", "14d"),
    DAY_21("21d", "21d");

    private final String label;
    private final String value;

    SelectMenuOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}

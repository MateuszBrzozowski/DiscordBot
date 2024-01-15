package pl.mbrzozowski.ranger.disboard;

public enum ReminderMode {

    DISABLE(0),
    ONCE_A_DAY(1),
    TWICE_A_DAY(2),
    THREE_TIMES_A_DAY(3),
    EVERY_TWO_HOURS(9);

    private final int mode;
    private static final ReminderMode[] ENUMS = ReminderMode.values();

    ReminderMode(int mode) {
        this.mode = mode;
    }

    public static ReminderMode of(int mode) {
        if (mode == 9) {
            return ENUMS[4];
        }
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Invalid value for disboard reminder mode (disboard.reminder.mode=" + mode + ")");
        }
        return ENUMS[mode];
    }

    public int getMode() {
        return mode;
    }
}

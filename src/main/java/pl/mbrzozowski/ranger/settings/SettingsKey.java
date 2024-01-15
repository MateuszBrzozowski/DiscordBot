package pl.mbrzozowski.ranger.settings;

public enum SettingsKey {

    DISBOARD_REMINDER_MODE("disboard.reminder.mode"),
    DISBOARD_REMINDER_DATE("disboard.reminder.date"),
    DISBOARD_REMINDER_DATE_ANSWER("disboard.reminder.date.answer"),
    DISBOARD_REMINDER_COUNT_FOR_DAY("disboard.reminder.count"),
    ;

    private final String key;

    SettingsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

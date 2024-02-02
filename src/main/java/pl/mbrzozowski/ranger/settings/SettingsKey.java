package pl.mbrzozowski.ranger.settings;

public enum SettingsKey {

    DISBOARD_REMINDER_MODE("disboard.reminder.mode"),
    DISBOARD_REMINDER_DATE("disboard.reminder.date"),
    DISBOARD_REMINDER_DATE_ANSWER("disboard.reminder.date.answer"),
    DISBOARD_REMINDER_COUNT_FOR_DAY("disboard.reminder.count"),
    SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS("server.service.delete.channel.after"),
    SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS("server.service.close.channel.after"),
    RECRUIT_CHANNEL_DELETE_DELAY("recruit.channel.delete.after"),
    EVENT_CHANNEL_DELETE_DELAY("event.channel.delete.after"),
    EVENT_CHANNEL_TACTICAL_DELETE_DELAY("event.channel.tactical.delete.after");

    private final String key;

    SettingsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

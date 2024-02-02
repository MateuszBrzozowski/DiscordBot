package pl.mbrzozowski.ranger.settings;

public enum SettingsKey {

    DISBOARD_REMINDER_MODE("disboard.reminder.mode"),
    DISBOARD_REMINDER_DATE("disboard.reminder.date"),
    DISBOARD_REMINDER_DATE_ANSWER("disboard.reminder.date.answer"),
    DISBOARD_REMINDER_COUNT_FOR_DAY("disboard.reminder.count"),
    SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS("server.service.delete.channel.after.days"),
    SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS("server.service.close.channel.after.days"),
    RECRUIT_CHANNEL_DELETE_DELAY("recruit.channel.cleaning"),
    EVENT_CHANNEL_DELETE_DELAY("event.channel.cleaning"),
    EVENT_CHANNEL_TACTICAL_DELETE_DELAY("event.channel.tactical.cleaning");

    private final String key;

    SettingsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

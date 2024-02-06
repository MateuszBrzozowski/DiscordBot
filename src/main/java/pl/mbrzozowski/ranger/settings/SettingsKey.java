package pl.mbrzozowski.ranger.settings;

public enum SettingsKey {

    DISBOARD_REMINDER_MODE("disboard.reminder.mode"),
    DISBOARD_REMINDER_DATE("disboard.reminder.date"),
    DISBOARD_REMINDER_DATE_ANSWER("disboard.reminder.date.answer"),
    DISBOARD_REMINDER_COUNT_FOR_DAY("disboard.reminder.count"),
    SERVER_SERVICE_DELETE_CHANNEL("server.service.delete.channel.after"),
    SERVER_SERVICE_CLOSE_CHANNEL("server.service.close.channel.after"),
    RECRUIT_DELETE_CHANNEL_DELAY("recruit.channel.delete.after"),
    EVENT_DELETE_CHANNEL_DELAY("event.channel.delete.after"),
    EVENT_DELETE_CHANNEL_TACTICAL_DELAY("event.channel.tactical.delete.after"),
    SEED_CALL_LIVE_AMOUNT("seed.call.live.amount"),
    SEED_CALL_SQUAD_AMOUNT("seed.call.squad.amount");

    private final String key;

    SettingsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

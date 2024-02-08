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
    SEED_CALL_LEVEL_ONE("seed.call.level.1"),
    SEED_CALL_LEVEL_TWO("seed.call.level.2"),
    SEED_CALL_LEVEL_THREE("seed.call.level.3"),
    SEED_CALL_LEVEL_FOUR("seed.call.level.4"),
    SEED_CALL_LEVEL_ONE_COUNT("seed.call.level.1.count"),
    SEED_CALL_LEVEL_TWO_COUNT("seed.call.level.2.count"),
    SEED_CALL_LEVEL_THREE_COUNT("seed.call.level.3.count"),
    SEED_CALL_LEVEL_FOUR_COUNT("seed.call.level.4.count"),
    SEED_CALL("seed.call.enable"),
    SEED_CALL_LEVEL_ONE_CONDITIONS("seed.call.level.1.conditions"),
    SEED_CALL_LEVEL_TWO_CONDITIONS("seed.call.level.2.conditions"),
    SEED_CALL_LEVEL_THREE_CONDITIONS("seed.call.level.3.conditions"),
    SEED_CALL_LEVEL_FOUR_CONDITIONS("seed.call.level.4.conditions"),
    SEED_CALL_LAST("seed.call.last"),
    SEED_CALL_LEVEL("seed.call.level"),
    SEED_CALL_CHANNEL_ID("seed.call.channel"),
    STATS_DATE_FROM("stats-date");

    private final String key;

    SettingsKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

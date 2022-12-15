package pl.mbrzozowski.ranger.helpers;

public interface Commands {

    String PREFIX = "!";
    String SUFFIX = "test";

    String REMINDER_OFF = PREFIX + "reminder Off" + SUFFIX;
    String REMINDER_ON = PREFIX + "reminder On" + SUFFIX;
    String CANCEL = PREFIX + "cancel" + SUFFIX;
    String DISABLE_BUTTONS = PREFIX + "disable" + SUFFIX;
    String ENABLE_BUTTONS = PREFIX + "enable" + SUFFIX;
    String EVENT = PREFIX + "event" + SUFFIX;
    String DICE = PREFIX + "kostka" + SUFFIX;
    String HELPS = PREFIX + "help" + SUFFIX;
    String CLOSE = PREFIX + "close" + SUFFIX;
    String START_REKRUT = PREFIX + "startRekrut" + SUFFIX;
    String POSITIVE = PREFIX + "p" + SUFFIX;
    String NEGATIVE = PREFIX + "n" + SUFFIX;
    String EVENTS_SETTINGS = PREFIX + "eventsEdit" + SUFFIX;
    String MSG = PREFIX + "msg" + SUFFIX;
    String MSG_CANCEL = PREFIX + "msgCancel" + SUFFIX;
    String SQUAD_SEEDERS_INFO = PREFIX + "squadSeedersInfo" + SUFFIX;
    String EMBED_SERVER_SERVICE = PREFIX + "ServerService" + SUFFIX;
    String RECRUIT_OPINIONS = PREFIX + "rekrutopinia" + SUFFIX;
    String UPDATE_WL = PREFIX + "updateWL" + SUFFIX;
    //ROLE
    String ROLES = PREFIX + "roles" + SUFFIX;
}

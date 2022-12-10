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
    String STATS = PREFIX + "stats" + SUFFIX;
    String STATS_PROFILE = PREFIX + "profile" + SUFFIX;
    String RECRUT_OPINIONS = PREFIX + "rekrutopinia" + SUFFIX;
    String UPDATE_WL = PREFIX + "updateWL" + SUFFIX;
    //ROLE
    String ROLE = "role.";
    String ROLES = PREFIX + "roles" + SUFFIX;
    String TARKOV = PREFIX + ROLE + "tarkov" + SUFFIX;
    String VIRTUAL_REALITY = PREFIX + ROLE + "vr" + SUFFIX;
    String SQUAD = PREFIX + ROLE + "squad" + SUFFIX;
    String CS = PREFIX + ROLE + "cs" + SUFFIX;
    String WAR_THUNDER = PREFIX + ROLE + "wt" + SUFFIX;
    String MINECRAFT = PREFIX + ROLE + "minecraft" + SUFFIX;
    String RAINBOW_SIX = PREFIX + ROLE + "r6" + SUFFIX;
    String WARGAME = PREFIX + ROLE + "wargame" + SUFFIX;
    String ARMA = PREFIX + ROLE + "arma" + SUFFIX;
}

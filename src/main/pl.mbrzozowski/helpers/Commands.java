package helpers;

public interface Commands {

    String PREFIX = "!";
    String SUFFIX = "test";

    String NO_NOTIFI = "-noNotifi" + SUFFIX;
    String REMINDER_OFF = PREFIX + "reminder Off" + SUFFIX;
    String REMINDER_ON = PREFIX + "reminder On" + SUFFIX;
    String CANCEL = PREFIX + "cancel" + SUFFIX;
    String STATUS = PREFIX + "status" + SUFFIX;
    String DATE = PREFIX + "date" + SUFFIX;
    String TIME = PREFIX + "time" + SUFFIX;
    String DISABLE_BUTTONS = PREFIX + "disable" + SUFFIX;
    String ENABLE_BUTTONS = PREFIX + "enable" + SUFFIX;
    String CANCEL_EVENT = PREFIX + "cancelEvent" + SUFFIX;
    String REMOVE_USER_FROM_EVENT = PREFIX + "removeUserFromEvent" + SUFFIX;
    String REMOVE_USER_FROM_EVENTS = PREFIX + "removeUserFromEvent" + SUFFIX;
    String GENERATOR_HERE = PREFIX + "generatorHere" + SUFFIX;
    String GENERATOR = PREFIX + "generator" + SUFFIX;
    String DICE = PREFIX + "kostka" + SUFFIX;
    String HELPS = PREFIX + "help" + SUFFIX;
    String REOPEN = PREFIX + "open" + SUFFIX;
    String CLOSE = PREFIX + "close" + SUFFIX;
    String START_REKRUT = PREFIX + "startRekrut" + SUFFIX;
    String POSITIVE = PREFIX + "p" + SUFFIX;
    String NEGATIVE = PREFIX + "n" + SUFFIX;
    String NEW_EVENT_HERE = PREFIX + "zapisyhere" + SUFFIX;
    String EVENTS_SETTINGS = PREFIX + "events" + SUFFIX;
    String MSG = PREFIX + "msg" + SUFFIX;
    String MSG_CANCEL = PREFIX + "msgCancel" + SUFFIX;
    String QUESTIONNAIRE = PREFIX + "ankieta" + SUFFIX;
    String QUESTIONNAIRE_MULTIPLE = PREFIX + "ankietaW" + SUFFIX;
    String QUESTIONNAIRE_PUBLIC = PREFIX + "ankietaP" + SUFFIX;
    String QUESTIONNAIRE_PUBLIC_MULTIPLE = PREFIX + "ankietaPW" + SUFFIX;
    String QUESTIONNAIRE_MULTIPLE_PUBLIC = PREFIX + "ankietaWP" + SUFFIX;
    String TOP_THREE = PREFIX + "top" + SUFFIX;
    String TOP_TEN = PREFIX + "topTen" + SUFFIX;
    String COUNT = PREFIX + "showMe" + SUFFIX;
    String SQUAD_SEEDERS_INFO = PREFIX + "squadSeedersInfo" + SUFFIX;
    String EMBED_SERVER_SERVICE = PREFIX + "ServerService" + SUFFIX;
    String STATS = PREFIX + "stats" + SUFFIX;
    String STATS_PROFILE = PREFIX + "profile" + SUFFIX;
    String SERVER_RULES = PREFIX + "serverrules" + SUFFIX;
    String STATS_MAPS = PREFIX + "mapstats" + SUFFIX;
    String RECRUT_OPINIONS = PREFIX + "rekrutopinia" + SUFFIX;
    String STATS_LAST_TEN_MAPS = PREFIX + "maps" + SUFFIX;
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

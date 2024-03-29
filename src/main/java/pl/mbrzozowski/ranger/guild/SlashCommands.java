package pl.mbrzozowski.ranger.guild;

public enum SlashCommands {
    ROLE("role", "Add/Remove a role by selecting it."),
    STEAM_PROFILE("profile", "Link your discord account to your steam profile if you want view stats from our server."),
    STEAM_PROFILE_64("steam64id", "Your steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/"),
    STATS("stats", "To view your stats from our server"),
    DAILY_STATS_SWITCH("stats-daily-switch", "Włącz lub wyłącz dzienne statystyki"),
    STATS_DATE("stats-date", "Ustaw datę od kiedy mają być pobierane dane"),
    DICE("kostka", "Rzut kością do gry"),
    COIN("moneta", "Rzut monetą"),
    ESSA("essa", "Sprawdza twój dzisiejszy poziom essy"),
    REP("rep", "Twoje punkty reputacji"),
    TOP_REP("rep-top", "Top 10 użytkowników pod względem pkt reputacji"),
    RANDOM_TIMEOUT("ruleta", "Kto nie gra ten nie wygrywa darmowego TIMEOUT'a"),
    RANGER_OF_THE_DAY("ranger-of-the-day", "Wybiera Clan membera dnia"),
    BIRTHDAY("birthday", "Wyświetla najbliższe urodziny clan membera"),
    BIRTHDAY_SET("birthday-set", "Ustawia date urodzenia"),
    BIRTHDAY_ADMIN_SET("birthday-admin-set", "Ustawia date urodzenia użytkownika"),
    BIRTHDAY_DAY("dzień", "Dzień urodzenia"),
    BIRTHDAY_MONTH("miesiąc", "Miesiąc urodzenia"),
    BIRTHDAY_YEAR("rok", "Rok urodzenia"),
    USER_ID("user-id", "User ID"),

    DISCORD_ROLE_OPTION_NAME_ID("id", "Discord ID dodawanej roli"),
    DISCORD_ROLE_OPTION_NAME_NAME("nazwa", "Nazwa wyświetlana na liście"),
    ADD_ROLE_TO_RANGER("add-role-to-ranger", "Dodaje nową rolę do Ranger bota dzięki czemu użytkownicy serwera będą mogli sobie ją sami przypisać."),
    REMOVE_ROLE_FROM_RANGER("remove-role-from-ranger", "Usuwa rolę z Ranger bota. Użytkownik serwera nie będzie mógł samemu przypisać sobie usuniętej roli."),
    GIVEAWAY_CREATE("gcreate", "Tworzy giveaway na tym kanale"),
    GIVEAWAY_END("gend", "Kończy giveaway i losuje nagrody"),
    GIVEAWAY_CANCEL("gcancel", "Kończy giveaway bez losowania nagród"),
    GIVEAWAY_LIST("glist", "Pokazuje listę aktywnych giveawayów na serwerze"),
    GIVEAWAY_RE_ROLL("greroll", "Losowanie nagród dla zakończonego giveawaya"),
    GIVEAWAY_ID("id", "Giveaway ID"),
    FIX_EVENT_EMBED("fix-event-embed", "Przywraca embed dla eventu"),
    FIX_GIVEAWAY_EMBED("fix-giveaway-embed", "Przywraca embed dla giveawaya"),
    EVENT_CREATE("event-create", "Otwiera generator eventów"),
    EVENT_SETTINGS("events-settings", "Otwiera ustawienia eventów"),
    RECRUIT_BLACK_LIST_ADD("rekrut-black-list-add", "Dodaje osobę na czarną listę. Nie będzie mogła złożyć podania"),
    RECRUIT_BLACK_LIST_REMOVE("rekrut-black-list-remove", "Usuwa osobę z czarnej listy."),
    RECRUIT_BLACK_LIST_INFO("rekrut-black-list-info", "Wyświetla informacje o użytkowniku na liście"),
    RECRUIT_DELETE_CHANNEL_DELAY("rekrut-delete-channel", "Ustawia czas po którym kanał rekrutacyjny zostanie usunięty"),
    EVENT_DELETE_CHANNEL_DELAY("event-delete-channel-after", "Ustawia czas po którym event zostanie usunięty"),
    EVENT_DELETE_CHANNEL_TACTICAL_DELAY("event-delete-channel-tact-after", "Ustawia czas po którym event dla grupy taktycznej zostanie usunięty"),
    SERVER_SERVICE_DELETE_CHANNEL("server-delete-channel-after", "Ustawia czas po którym ticket zostanie usunięty. Ilość dni od zamknięcia"),
    SERVER_SERVICE_CLOSE_CHANNEL("server-close-channel-after", "Ustawia czas po którym ticket zostanie zamknięty. Ilość dni od ostatniej aktywności"),
    RANK_ROLE_FIND_BY_NAME("rank-role-find-by-name", "Wyszukuję nazwę roli po nazwie"),
    RANK_ROLE_FIND_BY_DISCORD_ID("rank-role-find-by-id", "Wyszukuję nazwę roli po discord id"),
    RANK_ROLE_ADD("rank-role-add", "Dodaje rolę stopnia do bota. Umożliwia automatyczne nadawanie ról."),
    RANK_ROLE_REMOVE("rank-role-remove", "Usuwa rolę stopnia z bota."),
    SEED_CALL_CONDITIONS("seed-call-warunek-dodaj", "Jeżeli liczba graczy była większa od [] przez ostatnie [] minut. Offset(5)"),
    SEED_CALL_CONDITIONS_REMOVE("seed-call-warunek-usuń", "Usuwa warunek"),
    SEED_CALL_MESSAGE("seed-call-wiadomość-dodaj", "Dodaje wiadomość do levelu"),
    SEED_CALL_MESSAGE_REMOVE("seed-call-wiadomość-usuń", "Usuwa wiadomość do levelu"),
    SEED_CALL_MESSAGE_INFO("seed-call-wiadomości", "Wyświetla ustawione wiadomości dla levelu"),
    SEED_CALL_AMOUNT("seed-call-ilość", "Maksymalna ilość wiadomości na dzień o Live. 0 - wyłącza."),
    SEED_CALL_ENABLE("seed-call", "Włącza lub wyłącza seed call service"),
    SEED_CALL_CONDITIONS_INFO("seed-call-warunki", "Wyświetla istniejące warunki"),
    SEED_CALL_ROLE_ADD("seed-call-ustaw-role", "Dodaj role ID dla levelu którą mam oznaczyć"),
    SEED_CALL_ROLE_REMOVE("seed-call-usuń-role", "Usuwa role z levelu"),
    SEED_CALL_CHANNEL("seed-call-kanał", "Ustawia kanał na którym mają być wysyłane wiadomości"),
    SEED_CALL_REPLACEMENT_LEVELS("seed-call-zmiana-leveli", "Zamienia ze sobą levele"),
    ROLE_ID("role-id", "ID Roli"),
    LEVEL("level", "Level"),
    LEVEL_SECOND("level-second", "Level"),
    COUNT("count", "Ilość");

    private final String name;
    private final String description;

    SlashCommands(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

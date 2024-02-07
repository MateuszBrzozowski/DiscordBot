package pl.mbrzozowski.ranger.helpers;

public enum SlashCommands {
    ROLE("role", "Add/Remove a role by selecting it."),
    STEAM_PROFILE("profile", "Link your discord account to your steam profile if you want view stats from our server."),
    STEAM_PROFILE_64("steam64id", "Your steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/"),
    STATS("stats", "To view your stats from our server"),
    DICE("kostka", "Rzut kością do gry"),
    COIN("moneta", "Rzut monetą"),
    ESSA("essa", "Sprawdza twój dzisiejszy poziom essy"),

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
    SEED_CALL_AMOUNT("seed-call-ilość", "Maksymalna ilość wiadomości na dzień o Live. 0 - wyłącza."),
    SEED_CALL_ENABLE("seed-call", "Włącza lub wyłącza seed call service"),
    SEED_CALL_CONDITIONS_INFO("seed-call-warunki", "Wyświetla istniejące warunki"),
    TYPE("type", "Typ"),
    COUNT("count", "Ilość"),
    LIVE("live", "Ilość"),
    PING_SQUAD("ping-squad", "Ilość");

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

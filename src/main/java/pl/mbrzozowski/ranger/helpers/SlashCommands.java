package pl.mbrzozowski.ranger.helpers;

public enum SlashCommands {
    ADD_ROLE_TO_RANGER("add-role-to-ranger","Dodaje nową rolę do Ranger bota dzięki czemu użytkownicy serwera będą mogli sobie ją sami przypisać."),
    REMOVE_ROLE_FROM_RANGER("remove-role-from-ranger","Usuwa rolę z Ranger bota. Użytkownik serwera nie będzie mógł samemu przypisać sobie usuniętej roli."),
    DISCORD_ROLE_OPTION_NAME_ID("id","Discord ID dodawanej roli"),
    DISCORD_ROLE_OPTION_NAME_NAME("nazwa","Nazwa wyświetlana na liście"),
    STEAM_PROFILE("profile","Link your discord account to your steam profile if you want view stats from our server."),
    STEAM_PROFILE_64("steam64id","Your steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/"),
    STATS("stats","To view your stats from our server"),
    DICE("kostka","Rzut kością do gry"),
    COIN("moneta","Rzut monetą"),
    ESSA("essa","Sprawdza twój dzisiejszy poziom essy"),
    GIVEAWAY_CREATE("gcreate","Tworzy giveaway na tym kanale"),
    GIVEAWAY_END("gend","Kończy giveaway i losuje nagrody"),
    GIVEAWAY_CANCEL("gcancel","Kończy giveaway bez losowania nagród"),
    GIVEAWAY_LIST("glist","Pokazuje listę aktywnych giveawayów na serwerze"),
    GIVEAWAY_RE_ROLL("greroll","Losowanie nagród dla zakończonego giveawaya"),
    GIVEAWAY_ID("id","Giveaway ID"),
    FIX_EVENT_EMBED("fix-event-embed","Przywraca embed dla eventu"),
    FIX_GIVEAWAY_EMBED("fix-giveaway-embed","Przywraca embed dla giveawaya"),
    EVENT_CREATE("event-create","Otwiera generator eventów"),
    EVENT_SETTINGS("events-settings","Otwiera ustawienia eventów"),
    ROLE("role","Add/Remove a role by selecting it."),
    RECRUIT_BLACK_LIST_ADD("rekrut-black-list-add","Dodaje osobę na czarną listę. Nie będzie mogła złożyć podania"),
    RECRUIT_BLACK_LIST_REMOVE("rekrut-black-list-remove","Usuwa osobę z czarnej listy."),
    RECRUIT_BLACK_LIST_INFO("rekrut-black-list-info","Wyświetla informacje o użytkowniku na liście"),
    RECRUIT_DELETE_CHANNEL_DELAY("rekrut-delete-channel","Ustawia czas po którym kanał rekrutacyjny zostanie usunięty"),
    EVENT_DELETE_CHANNEL_DELAY("event-delete-channel-after", "Ustawia czas po którym event zostanie usunięty");

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

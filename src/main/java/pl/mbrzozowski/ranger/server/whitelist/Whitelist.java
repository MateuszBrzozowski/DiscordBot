package pl.mbrzozowski.ranger.server.whitelist;

import java.util.List;

public class Whitelist {

    private static List<Player> players;
    private static final int MIN_HOURS = 30;
    protected static final int MIN_SECONDS = MIN_HOURS * 60 * 60; //godziny wyrażone w sekundach.

    public static void whitelistUpdate() {
        Battlemetrics battlemetrics = new Battlemetrics();
        players = battlemetrics.getPlayers();
        if (players != null) {
            System.out.println("Pobrano " + players.size() + " graczy z BM leaderboard");
            GoogleSheet googleSheet = new GoogleSheet(players);
            googleSheet.updatePlayers();
        } else {
            System.out.println("Pobrano " + 0 + " graczy z BM leaderboard");
        }

    }
}

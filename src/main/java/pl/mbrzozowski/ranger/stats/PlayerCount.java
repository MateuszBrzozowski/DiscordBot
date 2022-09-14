package pl.mbrzozowski.ranger.stats;

public class PlayerCount {

    private final String playerName;
    private int count;

    public PlayerCount(String playerName) {
        this.playerName = playerName;
        this.count = 1;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCount() {
        return count;
    }

    public void addCount() {
        count += 1;
    }
}

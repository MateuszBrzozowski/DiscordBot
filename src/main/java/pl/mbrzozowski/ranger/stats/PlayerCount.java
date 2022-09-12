package pl.mbrzozowski.ranger.stats;

public class PlayerCount extends Player {

    private String playerName;
    private int count;

    public PlayerCount(String userDiscordID, String steamID) {
        super(userDiscordID, steamID);
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCount() {
        return count;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

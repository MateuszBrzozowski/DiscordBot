package stats;

public class Player {

    private String steamID;
    private String userDiscordID;

    public Player(String userDiscordID, String steamID) {
        this.steamID = steamID;
        this.userDiscordID = userDiscordID;
    }

    String getSteamID() {
        return steamID;
    }

    String getUserDiscordID() {
        return userDiscordID;
    }
}

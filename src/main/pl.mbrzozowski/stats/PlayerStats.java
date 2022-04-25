package stats;

public class PlayerStats extends Player{

    public PlayerStats(String userDiscordID, String steamID) {
        super(userDiscordID, steamID);
    }

    private float kd;
    private int kills;
    private int deaths;
    private int wounds;
    private int revives;
    private int revivesYou;
    private int teamkills;
    private String gun;
}

package stats;

public class PlayerStats extends Player {

    private String profileName;
    private float kd;
    private int kills;
    private int deaths;
    private int wounds;
    private int revives;
    private int revivesYou;
    private int teamkills;
    private String gun;

    public PlayerStats(Player player) {
        super(player.getUserDiscordID(), player.getSteamID());
    }

    public PlayerStats setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }
}

package stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.ArrayList;

public class PlayerStats extends Player {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private String profileName;
    private float kd;
    private int kills;
    private int deaths;
    private int wounds;
    private int revives;
    private int revivesYou;
    private int teamkills;
    private float effectiveness;
    private String gun;
//    private ArrayList<Gun> guns = new ArrayList<>();

    public PlayerStats(Player player) {
        super(player.getUserDiscordID(), player.getSteamID());
    }

    public PlayerStats setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public PlayerStats setKd() {
        float kills = getKills();
        float deaths = getDeaths();
        this.kd = kills / deaths;
        return this;
    }

    public PlayerStats setKills(int kills) {
        this.kills = kills;
        return this;
    }

    public PlayerStats setDeaths(int deaths) {
        this.deaths = deaths;
        return this;
    }

    public PlayerStats setWounds(int wounds) {
        this.wounds = wounds;
        return this;
    }

    public PlayerStats setRevives(int revives) {
        this.revives = revives;
        return this;
    }

    public PlayerStats setRevivesYou(int revivesYou) {
        this.revivesYou = revivesYou;
        return this;
    }

    public PlayerStats setTeamkills(int teamkills) {
        this.teamkills = teamkills;
        return this;
    }

    public PlayerStats setGuns(ArrayList<Gun> guns) {
        for (Gun g : guns) {
            logger.info(g.getName() + "  " + g.getCount());
        }
        for (int i = 0; i < 3; i++) {
            setGun(guns.get(i).getName(), i);
        }
        return this;
    }

    public PlayerStats setMostKills(ArrayList<Player> players){
        return this;
    }

    private void setGun(String gun, int index) {
        if (index == 0) {
            this.gun = "**" + (index + 1) + "." + gun + "**\n";
        } else {
            this.gun += (index + 1) + "." + gun + "\n";
        }

    }

    public PlayerStats setEffectiveness() {
        float kills = getKills();
        float wounds = getWounds();
        this.effectiveness = kills / wounds;
        return this;
    }

    public String getProfileName() {
        return profileName;
    }

    public float getKd() {
        return kd;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getWounds() {
        return wounds;
    }

    public int getRevives() {
        return revives;
    }

    public int getRevivesYou() {
        return revivesYou;
    }

    public int getTeamkills() {
        return teamkills;
    }

    public String getGun() {
        return gun;
    }

    public float getEffectiveness() {
        return effectiveness;
    }
}

package ranger.stats;

import java.util.ArrayList;

public class PlayerStats extends Player {

    private String profileName;
    private float kd = 0;
    private int kills = 0;
    private int deaths = 0;
    private int wounds = 0;
    private int revives = 0;
    private int revivesYou = 0;
    private int teamkills = 0;
    private float effectiveness = 0;
    private String weapon = "-";
    private String mostKills = "-";
    private String mostKilledBy = "-";
    private String mostRevives = "-";
    private String mostRevivedBy = "-";

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

    public PlayerStats setWeapon(ArrayList<Gun> guns) {
        if (guns.size() > 0) {
            for (int i = 0; i < 3; i++) {
                setWeapon(guns.get(i).getName(), i);
            }
        } else {
            this.weapon = "-";
        }

        return this;
    }

    public PlayerStats setMostKills(ArrayList<PlayerCount> players) {
        if (players.size() > 0) {
            removeNull(players);
            for (int i = 0; i < 1; i++) {
                setMostKills(players.get(i).getPlayerName(), i, players.get(i).getCount());
            }
        } else {
            this.mostKills = "-";
        }

        return this;
    }

    private void setMostKills(String playerName, int index, int count) {
        if (index == 0) {
            this.mostKills = "**" + playerName + "**- (" + count + ")\n";
        } else {
            this.mostKills += (index + 1) + "." + playerName + "- (" + count + ")\n";
        }
    }

    public PlayerStats setMostKilledBy(ArrayList<PlayerCount> players) {
        if (players.size() > 0) {
            removeNull(players);
            for (int i = 0; i < 1; i++) {
                setMostKilledBy(players.get(i).getPlayerName(), i, players.get(i).getCount());
            }
        } else {
            this.mostKilledBy = "-";
        }
        return this;
    }

    private void setMostKilledBy(String playerName, int index, int count) {
        if (index == 0) {
            this.mostKilledBy = "**" + playerName + "**- (" + count + ")\n";
        } else {
            this.mostKilledBy += (index + 1) + "." + playerName + "- (" + count + ")\n";
        }
    }

    public PlayerStats setMostRevives(ArrayList<PlayerCount> players) {
        if (players.size() > 0) {
            removeNull(players);
            for (int i = 0; i < 1; i++) {
                setMostRevives(players.get(i).getPlayerName(), i, players.get(i).getCount());
            }
        } else {
            this.mostRevives = "-";
        }
        return this;
    }

    private void setMostRevives(String playerName, int index, int count) {
        if (index == 0) {
            this.mostRevives = "**" + playerName + "**- (" + count + ")\n";
        } else {
            this.mostRevives += (index + 1) + "." + playerName + "- (" + count + ")\n";
        }
    }

    public PlayerStats setMostRevivedBy(ArrayList<PlayerCount> players) {
        if (players.size() > 0) {
            removeNull(players);
            for (int i = 0; i < 1; i++) {
                setMostRevivedBy(players.get(i).getPlayerName(), i, players.get(i).getCount());
            }
        } else {
            this.mostRevivedBy = "-";
        }
        return this;
    }

    private void setMostRevivedBy(String playerName, int index, int count) {
        if (index == 0) {
            this.mostRevivedBy = "**" + playerName + "**- (" + count + ")\n";
        } else {
            this.mostRevivedBy += (index + 1) + "." + playerName + "- (" + count + ")\n";
        }
    }

    private void removeNull(ArrayList<PlayerCount> players) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerName() == null) {
                players.remove(i);
            }
        }
    }

    private void setWeapon(String gun, int index) {
        if (index == 0) {
            this.weapon = "**" + (index + 1) + "." + gun + "**\n";
        } else {
            this.weapon += (index + 1) + "." + gun + "\n";
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

    public String getWeapon() {
        return weapon;
    }

    public float getEffectiveness() {
        return effectiveness;
    }

    public String getMostKills() {
        return mostKills;
    }

    public String getMostKilledBy() {
        return mostKilledBy;
    }

    public String getMostRevives() {
        return mostRevives;
    }

    public String getMostRevivedBy() {
        return mostRevivedBy;
    }
}

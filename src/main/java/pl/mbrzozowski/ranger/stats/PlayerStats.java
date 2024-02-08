package pl.mbrzozowski.ranger.stats;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
@Builder
public class PlayerStats {

    private String profileName;
    private double kd = 0;
    private int kills = 0;
    private int deaths = 0;
    private int wounds = 0;
    private int revives = 0;
    private int teamKills = 0;
    private int revivesYou = 0;
    private double effectiveness = 0;
    private double kdLast7Days = 0;
    private double kdLast30Days = 0;
    private double kdLast90Days = 0;
    private double kdCurrentYear = 0;
    private double kdLastYear = 0;
    private String weapon = "-";
    private String mostKills = "-";
    private String mostRevives = "-";
    private String mostKilledBy = "-";
    private String mostRevivedBy = "-";

    private PlayerStats() {
    }

    private PlayerStats(String profileName,
                        double kd,
                        int kills,
                        int deaths,
                        int wounds,
                        int revives,
                        int teamKills,
                        int revivesYou,
                        double effectiveness,
                        double kdLast7Days,
                        double kdLast30Days,
                        double kdLast90Days,
                        double kdCurrentYear,
                        double kdLastYear,
                        String weapon,
                        String mostKills,
                        String mostRevives,
                        String mostKilledBy,
                        String mostRevivedBy) {
        this.profileName = profileName;
        this.kd = kd;
        this.kills = kills;
        this.deaths = deaths;
        this.wounds = wounds;
        this.revives = revives;
        this.teamKills = teamKills;
        this.revivesYou = revivesYou;
        this.effectiveness = effectiveness;
        this.kdLast7Days = kdLast7Days;
        this.kdLast30Days = kdLast30Days;
        this.kdLast90Days = kdLast90Days;
        this.kdCurrentYear = kdCurrentYear;
        this.kdLastYear = kdLastYear;
        this.weapon = weapon;
        this.mostKills = mostKills;
        this.mostRevives = mostRevives;
        this.mostKilledBy = mostKilledBy;
        this.mostRevivedBy = mostRevivedBy;
    }

    public static class PlayerStatsBuilder {

        public PlayerStatsBuilder weapon(@NotNull ArrayList<Gun> guns) {
            if (guns.size() > 0) {
                for (int i = 0; i < 3; i++) {
                    if (guns.size() > i) {
                        setWeapon(guns.get(i), i);
                    }
                }
            } else {
                this.weapon = "-";
            }
            return this;
        }

        public PlayerStatsBuilder mostKills(@NotNull ArrayList<PlayerCount> players) {
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

        public PlayerStatsBuilder mostKilledBy(@NotNull ArrayList<PlayerCount> players) {
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

        public PlayerStatsBuilder mostRevives(@NotNull ArrayList<PlayerCount> players) {
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

        public PlayerStatsBuilder mostRevivedBy(@NotNull ArrayList<PlayerCount> players) {
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

        private void setMostKills(String playerName, int index, int count) {
            if (index == 0) {
                this.mostKills = "**" + playerName + "**- (" + count + ")\n";
            } else {
                this.mostKills += (index + 1) + "." + playerName + "- (" + count + ")\n";
            }
        }

        private void setMostKilledBy(String playerName, int index, int count) {
            if (index == 0) {
                this.mostKilledBy = "**" + playerName + "**- (" + count + ")\n";
            } else {
                this.mostKilledBy += (index + 1) + "." + playerName + "- (" + count + ")\n";
            }
        }

        private void setMostRevives(String playerName, int index, int count) {
            if (index == 0) {
                this.mostRevives = "**" + playerName + "**- (" + count + ")\n";
            } else {
                this.mostRevives += (index + 1) + "." + playerName + "- (" + count + ")\n";
            }
        }

        private void setMostRevivedBy(String playerName, int index, int count) {
            if (index == 0) {
                this.mostRevivedBy = "**" + playerName + "**- (" + count + ")\n";
            } else {
                this.mostRevivedBy += (index + 1) + "." + playerName + "- (" + count + ")\n";
            }
        }

        private void removeNull(@NotNull ArrayList<PlayerCount> players) {
            players.removeIf(playerCount -> playerCount.getPlayerName() == null);
        }

        private void setWeapon(Gun gun, int index) {
            if (index == 0) {
                this.weapon = "**" + (index + 1) + "." + gun.getName() + "(" + gun.getCount() + ")**\n";
            } else {
                this.weapon += (index + 1) + "." + gun.getName() + "(" + gun.getCount() + ")\n";
            }

        }

        public void effectiveness() {
            double kills = this.kills;
            double wounds = this.wounds;
            this.effectiveness = kills / wounds;
        }

        private void kd() {
            double kills = this.kills;
            double deaths = this.deaths;
            this.kd = kills / deaths;
        }

        PlayerStats build() {
            kd();
            effectiveness();
            return new PlayerStats(this.profileName, this.kd, this.kills, this.deaths, this.wounds, this.revives, this.teamKills,
                    this.revivesYou, this.effectiveness, this.kdLast7Days, this.kdLast30Days, this.kdLast90Days, this.kdCurrentYear,
                    this.kdLastYear, this.weapon, this.mostKills, this.mostRevives, this.mostKilledBy, this.mostRevivedBy);
        }
    }
}

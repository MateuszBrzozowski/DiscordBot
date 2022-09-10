package ranger.stats;

import ranger.database.DBConnector;
import ranger.database.DBFactory;
import ranger.database.DBType;
import ranger.database.Factory;
import ranger.helpers.RangerLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatsDatabase {

    private final String DISCORD_USERS = "discrduser";
    private final Factory factory = new DBFactory();
    private final DBConnector connector = factory.createDB(DBType.STATS);

    public void addConnectPlayer(Player player) {
        String query = "INSERT INTO " + DISCORD_USERS + " (`userID`,`steamID`) " +
                "VALUES (\"" + player.getUserDiscordID() + "\",\"" + player.getSteamID() + "\")";
        connector.executeQuery(query);
    }

    ResultSet pullAllConnectedUsers() {
        ResultSet resultSet = null;
        String query = "SELECT * FROM `" + DISCORD_USERS + "`";
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            RangerLogger.info("Brak tabeli " + DISCORD_USERS);
        }
        return resultSet;
    }

    public void removeConnectPlayer(Player player) {
        String query = "DELETE FROM `" + DISCORD_USERS + "` WHERE userID=\"" + player.getUserDiscordID() + "\"";
        connector.executeQuery(query);
    }

    public String pullProfileName(String steamID) {
        ResultSet resultSet = null;
        String profileName = null;
        String query = "SELECT lastName FROM `dblog_steamusers` WHERE steamID=\"" + steamID + "\"";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        profileName = resultSet.getString("lastName");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return profileName;
    }

    public int pullKills(String steamID) {
        ResultSet resultSet = null;
        int kills = 0;
        String query = "SELECT COUNT(*) FROM `dblog_deaths` WHERE attacker=\"" + steamID + "\"";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        kills = resultSet.getInt("COUNT(*)");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return kills;
    }

    public int pullDeaths(String steamID) {
        ResultSet resultSet = null;
        int deaths = 0;
        String query = "SELECT COUNT(*) FROM `dblog_deaths` WHERE victim=\"" + steamID + "\"";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        deaths = resultSet.getInt("COUNT(*)");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return deaths;
    }

    public int pullWounds(String steamID) {
        ResultSet resultSet = null;
        int wounds = 0;
        String query = "SELECT COUNT(*) FROM `dblog_wounds` WHERE attacker=\"" + steamID + "\"";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        wounds = resultSet.getInt("COUNT(*)");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return wounds;
    }

    public int pullRevives(String steamID) {
        ResultSet resultSet = null;
        int revives = 0;
        String query = "SELECT COUNT(*) FROM `dblog_revives` WHERE reviver=\"" + steamID + "\"";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        revives = resultSet.getInt("COUNT(*)");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return revives;
    }

    public int pullRevivesYou(String steamID) {
        ResultSet resultSet = null;
        int revives = 0;
        String query = "SELECT COUNT(*) FROM `dblog_revives` WHERE victim=\"" + steamID + "\"";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        revives = resultSet.getInt("COUNT(*)");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return revives;
    }

    public int pullTeamkills(String steamID) {
        ResultSet resultSet = null;
        int teamkills = 0;
        String query = "SELECT COUNT(*) FROM `dblog_wounds` WHERE attacker=\"" + steamID + "\" AND teamkill=1";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        teamkills = resultSet.getInt("COUNT(*)");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return teamkills;
    }

    public ArrayList<Gun> pullGuns(String steamID) {
        ResultSet resultSet = null;
        ArrayList<Gun> guns = new ArrayList<>();
        String query = "SELECT weapon, COUNT(*) FROM `dblog_wounds` WHERE attacker=\"" + steamID + "\"" +
                "GROUP BY `weapon` ORDER BY `COUNT(*)` DESC";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String gunName = resultSet.getString("weapon");
                        int count = resultSet.getInt("COUNT(*)");
                        Gun gun = new Gun(gunName, count);
                        guns.add(gun);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        return guns;
    }

    public ArrayList<PlayerCount> pullMostKills(String steamID) {
        ResultSet resultSet = null;
        ArrayList<PlayerCount> players = new ArrayList<>();
        String query = "SELECT victimName, victim, COUNT(*) FROM `dblog_deaths` WHERE attacker=\"" + steamID + "\"" +
                "AND attacker!=victim GROUP BY `victim` ORDER BY `COUNT(*)` DESC";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String victim = resultSet.getString("victim");
                        String victimName = resultSet.getString("victimName");
                        int count = resultSet.getInt("COUNT(*)");
                        PlayerCount player = new PlayerCount("", victim);
                        player.setPlayerName(victimName);
                        player.setCount(count);
                        players.add(player);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return players;
    }

    public ArrayList<PlayerCount> pullMostKilledBy(String steamID) {
        ResultSet resultSet = null;
        ArrayList<PlayerCount> players = new ArrayList<>();
        String query = "SELECT attackerName, attacker, COUNT(*) FROM `dblog_deaths` WHERE victim=\"" + steamID + "\"" +
                "AND attacker!=victim GROUP BY `attacker` ORDER BY `COUNT(*)` DESC";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String attacker = resultSet.getString("attacker");
                        String attackerName = resultSet.getString("attackerName");
                        int count = resultSet.getInt("COUNT(*)");
                        PlayerCount player = new PlayerCount("", attacker);
                        player.setPlayerName(attackerName);
                        player.setCount(count);
                        players.add(player);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return players;
    }

    public ArrayList<PlayerCount> pullMostRevives(String steamID) {
        ResultSet resultSet = null;
        ArrayList<PlayerCount> players = new ArrayList<>();
        String query = "SELECT victimName, victim, COUNT(*) FROM `dblog_revives` WHERE reviver=\"" + steamID + "\"" +
                "AND reviver!=victim GROUP BY `victim` ORDER BY `COUNT(*)` DESC";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String victim = resultSet.getString("victim");
                        String victimName = resultSet.getString("victimName");
                        int count = resultSet.getInt("COUNT(*)");
                        PlayerCount player = new PlayerCount("", victim);
                        player.setPlayerName(victimName);
                        player.setCount(count);
                        players.add(player);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return players;
    }

    public ArrayList<PlayerCount> pullMostRevivedBy(String steamID) {
        ResultSet resultSet = null;
        ArrayList<PlayerCount> players = new ArrayList<>();
        String query = "SELECT reviverName, reviver, COUNT(*) FROM `dblog_revives` WHERE victim=\"" + steamID + "\"" +
                "AND reviver!=victim GROUP BY `reviver` ORDER BY `COUNT(*)` DESC";
        resultSet = connector.executeSelect(query);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String reviver = resultSet.getString("reviver");
                        String reviverName = resultSet.getString("reviverName");
                        int count = resultSet.getInt("COUNT(*)");
                        PlayerCount player = new PlayerCount("", reviver);
                        player.setPlayerName(reviverName);
                        player.setCount(count);
                        players.add(player);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return players;
    }

    public ArrayList<MapWithCountStatistic> pullAllMaps() {
        ResultSet resultSet = null;
        ArrayList<MapWithCountStatistic> maps = new ArrayList<>();
        String query = "CREATE TEMPORARY TABLE IF NOT EXISTS temp_table SELECT dblog_matches.id, dblog_matches.mapClassname, AVG(dblog_playercounts.players) AS players " +
                "FROM `dblog_matches` JOIN `dblog_playercounts` ON dblog_matches.id = dblog_playercounts.match " +
                "WHERE players > 20 AND dblog_matches.layerClassname NOT LIKE \"%seed%\" " +
                "GROUP BY dblog_matches.id";
        String querySecond = "SELECT mapClassname, COUNT(*) FROM temp_table GROUP BY mapClassname ORDER BY COUNT(*) DESC";
        connector.executeQuery(query);
        resultSet = connector.executeSelect(querySecond);
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String name = resultSet.getString("mapClassname");
                        int count = resultSet.getInt("COUNT(*)");
                        MapWithCountStatistic map = new MapWithCountStatistic(name, count);
                        maps.add(map);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return maps;
    }

    public List<MapLayer> pullLastTenMaps() {
        List<MapLayer> maps = new ArrayList<>();
        String query = "CREATE TEMPORARY TABLE IF NOT EXISTS temp_table_last_map SELECT dblog_matches.id, dblog_matches.map, dblog_matches.layer, AVG(dblog_playercounts.players) AS players " +
                "FROM `dblog_matches` JOIN `dblog_playercounts` ON dblog_matches.id = dblog_playercounts.match " +
                "WHERE players > 20 " +
                "GROUP BY dblog_matches.id";
        String querySecond = "SELECT map, layer FROM temp_table_last_map ORDER BY id DESC LIMIT 10";
        connector.executeQuery(query);
        ResultSet resultSet = connector.executeSelect(querySecond);
        while (true) {
            try {
                if (!resultSet.next()) {
                    break;
                } else {
                    String map = resultSet.getString("map");
                    String layer = resultSet.getString("layer");
                    MapLayer mapLayer = new MapLayer(map, layer);
                    maps.add(mapLayer);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return maps;
    }
}
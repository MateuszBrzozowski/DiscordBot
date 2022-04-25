package stats;

import database.DBConnector;
import database.DBFactory;
import database.DBType;
import database.Factory;
import helpers.RangerLogger;

import java.sql.ResultSet;

public class StatsDatabase {

    private final String DISCORD_USERS = "discrduser";
    private Factory factory = new DBFactory();
    private DBConnector connector = factory.createDB(DBType.STATS);

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
}

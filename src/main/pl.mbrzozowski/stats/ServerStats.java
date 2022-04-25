package stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerStats {

    private List<Player> connectedPlayers = new ArrayList<>();

    public void initialize() {
        pullConnectedUsers();
    }

    public boolean isUserConnected(String userID) {
        for (Player p : connectedPlayers) {
            if (p.getUserDiscordID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    public boolean connectUserToSteam(String userID, String steamID) {
        if (steamID.length() == 17) {
            StatsDatabase database = new StatsDatabase();
            Player player = new Player(userID, steamID);
            if (isUserConnected(userID)) {
                database.removeConnectPlayer(player);
                removeConnectPlayer(userID);
            }
            connectedPlayers.add(player);
            database.addConnectPlayer(player);
            return true;
        } else {
            return false;
        }
    }

    private void removeConnectPlayer(String userID) {
        for (int i = 0; i < connectedPlayers.size(); i++) {
            if (connectedPlayers.get(i).getUserDiscordID().equalsIgnoreCase(userID)) {
                connectedPlayers.remove(i);
                return;
            }
        }
    }

    private void pullConnectedUsers() {
        StatsDatabase database = new StatsDatabase();
        ResultSet resultSet = database.pullAllConnectedUsers();
        connectedPlayers.clear();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String userID = resultSet.getString("userID");
                        String steamID = resultSet.getString("steamID");
                        Player player = new Player(userID, steamID);
                        connectedPlayers.add(player);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}

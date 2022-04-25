package stats;

import embed.EmbedSettings;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerStats {

    private List<Player> connectedPlayers = new ArrayList<>();

    public void initialize() {
        pullConnectedUsers();
    }

    public void viewStatsForUser(String userID, TextChannel channel) {
        pullStatsFromDatabase(connectedPlayers.get(getIndex(userID)));
        sendEmbedWithStats(userID, channel);
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

    private void sendEmbedWithStats(String userID, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLACK);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(Users.getUserNicknameFromID(userID) + " profile");
        builder.setDescription("Profile info:\n" +
                "NAZWA GRACZA Z GRY");
        builder.addBlankField(false);
        builder.addField("⚔ K/D", "0", true);
        builder.addField("\uD83D\uDDE1 Kills", "0", true);
        builder.addField("⚰ Deaths", "0", true);
        builder.addField("\uD83E\uDE78 Wounds", "0", true);
        builder.addField("⚕ Revives", "0", true);
        builder.addField("⚕ Revives you", "0", true);
        builder.addField("\uD83D\uDC9E Gun", "-", true);
        builder.addField("\uD83D\uDEAB TeamKills", "0", true);
        builder.addBlankField(false);
        builder.addField("Most kills", "-", true);
        builder.addField("Most kills by", "-", true);
        builder.addField("Most revives", "-", true);
        builder.addField("Most revives by", "-", true);
        channel.sendMessage(builder.build()).queue();
    }

    private void pullStatsFromDatabase(Player player) {
        StatsDatabase database = new StatsDatabase();
        String profileName = database.pullProfileName(player.getSteamID());
        PlayerStats playerStats = new PlayerStats(player);
    }

    private int getIndex(String userID) {
        for (int i = 0; i < connectedPlayers.size(); i++) {
            if (connectedPlayers.get(i).getUserDiscordID().equalsIgnoreCase(userID)) {
                return i;
            }
        }
        return -1;
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

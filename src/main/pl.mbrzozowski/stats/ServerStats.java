package stats;

import embed.EmbedInfo;
import embed.EmbedSettings;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ServerStats {

    private List<Player> connectedPlayers = new ArrayList<>();

    public void initialize() {
        pullConnectedUsers();
    }

    public void viewStatsForUser(String userID, TextChannel channel) {
        PlayerStats playerStats = pullStatsFromDatabase(connectedPlayers.get(getIndex(userID)));
        if (isPlayerData(playerStats)) {
            sendEmbedWithStats(userID, channel, playerStats);
        } else {
            EmbedInfo.noDataToShow(channel);
        }
    }

    private boolean isPlayerData(PlayerStats playerStats) {
        return playerStats.getKills() != 0 || playerStats.getDeaths() != 0 || playerStats.getWounds() != 0;
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

    private void sendEmbedWithStats(String userID, TextChannel channel, PlayerStats playerStats) {
        DecimalFormat df = new DecimalFormat("0.00");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLACK);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(Users.getUserNicknameFromID(userID) + " profile");
        builder.setDescription("**Profile info:**```yaml\n" + playerStats.getProfileName() + "\n```");
        builder.addBlankField(false);
        builder.addField("⚔ K/D", "**" + df.format(playerStats.getKd()) + "**", true);
        builder.addField("⚔ Kills/Wounds", "**" + df.format(playerStats.getEffectiveness()) + "** effectiveness", true);
        builder.addField("\uD83D\uDDE1 Kills", "**" + playerStats.getKills() + "** kill(s)", true);
        builder.addField("⚰ Deaths", "**" + playerStats.getDeaths() + "** death(s)", true);
        builder.addField("\uD83E\uDE78 Wounds", "**" + playerStats.getWounds() + "** wound(s)", true);
        builder.addField("⚕ Revives", "**" + playerStats.getRevives() + "** revive(s)", true);
        builder.addField("⚕ Revived", "**" + playerStats.getRevivesYou() + "** revive(s)", true);
        builder.addField("\uD83D\uDEAB TeamKills", "**" + playerStats.getTeamkills() + "** teamkill(s)", true);
        builder.addField("\uD83D\uDC9E Weapon", playerStats.getWeapon(), true);
        builder.addBlankField(false);
        builder.addField("Most kills", playerStats.getMostKills(), true);
        builder.addField("Most killed by", playerStats.getMostKilledBy(), true);
        builder.addField("Most revives", playerStats.getMostRevives(), true);
        builder.addField("Most revived by", playerStats.getMostRevivedBy(), true);
        builder.setFooter("Data from 8.04.2022r.");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    private PlayerStats pullStatsFromDatabase(Player player) {
        StatsDatabase database = new StatsDatabase();
        PlayerStats playerStats = new PlayerStats(player);
        playerStats.setProfileName(database.pullProfileName(player.getSteamID()));
        playerStats.setKills(database.pullKills(player.getSteamID()))
                .setDeaths(database.pullDeaths(player.getSteamID()))
                .setWounds(database.pullWounds(player.getSteamID()))
                .setRevives(database.pullRevives(player.getSteamID()))
                .setRevivesYou(database.pullRevivesYou(player.getSteamID()))
                .setTeamkills(database.pullTeamkills(player.getSteamID()))
                .setWeapon(database.pullGuns(player.getSteamID()))
                .setMostKills(database.pullMostKills(player.getSteamID()))
                .setMostKilledBy(database.pullMostKilledBy(player.getSteamID()))
                .setMostRevives(database.pullMostRevives(player.getSteamID()))
                .setMostRevivedBy(database.pullMostRevivedBy(player.getSteamID()))
                .setKd()
                .setEffectiveness();
        return playerStats;
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

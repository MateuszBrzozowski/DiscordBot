package pl.mbrzozowski.ranger.stats;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.hibernate.exception.SQLGrammarException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.stats.model.*;
import pl.mbrzozowski.ranger.stats.service.*;

import java.awt.*;
import java.sql.SQLSyntaxErrorException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.*;

@Service
@Slf4j
public class ServerStats {

    private final DeathsService deathsService;
    private final DiscordUserService discordUserService;
    private final RevivesService revivesService;
    private final SteamUsersService steamUsersService;
    private final WoundsService woundsService;

    public ServerStats(DeathsService deathsService,
                       DiscordUserService discordUserService,
                       RevivesService revivesService,
                       SteamUsersService steamUsersService,
                       WoundsService woundsService) {
        this.deathsService = deathsService;
        this.discordUserService = discordUserService;
        this.revivesService = revivesService;
        this.steamUsersService = steamUsersService;
        this.woundsService = woundsService;
    }

    public void stats(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        try {
            if (isUserConnected(event.getUser().getId())) {
                viewStatsForUser(event, event.getUser().getId(), event.getChannel().asTextChannel());
            } else {
                ResponseMessage.notConnectedAccount(event);
            }
        } catch (CannotCreateTransactionException | SQLGrammarException | InvalidDataAccessResourceUsageException |
                 SQLSyntaxErrorException exception) {
            log.error("Database Error: " + exception.getMessage());
            ResponseMessage.cannotConnectStatsDB(event);
        }
    }

    public void profile(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        try {
            OptionMapping steam64id = event.getOption(STEAM_PROFILE_64.getName());
            if (steam64id == null) {
                event.getHook().deleteOriginal().queue();
                return;
            }
            if (connectUserToSteam(event.getUser().getId(), steam64id.getAsString())) {
                ResponseMessage.connectSuccessfully(event);
            } else {
                ResponseMessage.connectUnSuccessfully(event);
            }
        } catch (CannotCreateTransactionException | SQLGrammarException | InvalidDataAccessResourceUsageException |
                 SQLSyntaxErrorException exception) {
            log.error("Database Error: " + exception.getMessage());
            ResponseMessage.cannotConnectStatsDB(event);
        }
    }

    public void viewStatsForUser(SlashCommandInteractionEvent event, String userId, TextChannel channel) {
        log.info("UserId: " + userId + " - stats");
        PlayerStats playerStats = pullStatsFromDB(userId);
        if (playerStats != null) {
            if (hasPlayerData(playerStats)) {
                sendEmbedWithStats(event, userId, playerStats);
            } else {
                sendEmbedWithNoStats(event, userId, playerStats);
            }
        } else {
            ResponseMessage.playerStatsIsNull(event);
        }
    }

    private boolean hasPlayerData(@NotNull PlayerStats playerStats) {
        return playerStats.getKills() != 0 || playerStats.getDeaths() != 0 || playerStats.getWounds() != 0;
    }

    public boolean isUserConnected(String userID) throws SQLSyntaxErrorException {
        Optional<DiscordUser> discordUser = discordUserService.findByUserId(userID);
        return discordUser.isPresent();
    }

    public boolean connectUserToSteam(String userID, String steamID) throws SQLSyntaxErrorException {
        if (steamID != null && steamID.length() == 17 && userID != null) {
            DiscordUser discordUser = new DiscordUser(userID, steamID);
            discordUserService.save(discordUser);
            return true;
        } else {
            return false;
        }
    }

    private void sendEmbedWithStats(@NotNull SlashCommandInteractionEvent event, String userID, @NotNull PlayerStats playerStats) {
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
        event.getHook().editOriginal("<@" + userID + ">").setEmbeds(builder.build()).queue();
        log.info("Embed with stats sent for user(id=" + userID + ")");
    }

    private void sendEmbedWithNoStats(@NotNull SlashCommandInteractionEvent event, String userID, @NotNull PlayerStats playerStats) {
        DecimalFormat df = new DecimalFormat("0.00");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLACK);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(Users.getUserNicknameFromID(userID) + " profile");
        builder.setDescription("**Profile info:**```yaml\n" + playerStats.getProfileName() + "\n```");
        builder.addBlankField(false);
        builder.addField("⚔ K/D", "**0**", true);
        builder.addField("⚔ Kills/Wounds", "**0** effectiveness", true);
        builder.addField("\uD83D\uDDE1 Kills", "**" + playerStats.getKills() + "** kill(s)", true);
        builder.addField("⚰ Deaths", "**" + playerStats.getDeaths() + "** death(s)", true);
        builder.addField("\uD83E\uDE78 Wounds", "**" + playerStats.getWounds() + "** wound(s)", true);
        builder.addField("⚕ Revives", "**" + playerStats.getRevives() + "** revive(s)", true);
        builder.addField("⚕ Revived", "**" + playerStats.getRevivesYou() + "** revive(s)", true);
        builder.addField("\uD83D\uDEAB TeamKills", "**" + playerStats.getTeamkills() + "** teamkill(s)", true);
        builder.addField("\uD83D\uDC9E Weapon", playerStats.getWeapon(), true);
        builder.addBlankField(false);
        builder.addField("Most kills", "-", true);
        builder.addField("Most killed by", playerStats.getMostKilledBy(), true);
        builder.addField("Most revives", playerStats.getMostRevives(), true);
        builder.addField("Most revived by", playerStats.getMostRevivedBy(), true);
        builder.setFooter("Data from 8.04.2022r.");
        event.getHook().editOriginal("<@" + userID + ">").setEmbeds(builder.build()).queue();
        log.info("Embed with no stats sent for user(id=" + userID + ")");
    }

    private @Nullable PlayerStats pullStatsFromDB(String userId) {
        Optional<DiscordUser> userOptional = discordUserService.findByUserId(userId);
        if (userOptional.isPresent()) {
            DiscordUser discordUser = userOptional.get();
            PlayerStats playerStats = new PlayerStats();
            playerStats.setSteamID(discordUser.getSteamID());
            playerStats.setUserDiscordID(discordUser.getUserID());

            Optional<SteamUsers> steamUsersOptional = steamUsersService.findBySteamId(discordUser.getSteamID());
            if (steamUsersOptional.isPresent()) {
                playerStats.setProfileName(steamUsersOptional.get().getLastName());
            } else {
                return null;
            }
            List<Deaths> deathsList = deathsService.findByAttackerOrVictim(discordUser.getSteamID(), discordUser.getSteamID());
            List<Revives> revivesList = revivesService.findByReviverOrVictim(discordUser.getSteamID(), discordUser.getSteamID());
            List<Wounds> woundsList = woundsService.findByAttackerOrVictim(discordUser.getSteamID(), discordUser.getSteamID());

            playerStats.setKills(getKills(deathsList, discordUser.getSteamID()))
                    .setDeaths(getDeaths(deathsList, discordUser.getSteamID()))
                    .setWounds(getWounds(woundsList, discordUser.getSteamID()))
                    .setRevives(getRevives(revivesList, discordUser.getSteamID()))
                    .setRevivesYou(getRevivesYou(revivesList, discordUser.getSteamID()))
                    .setTeamkills(getTeamKills(woundsList, discordUser.getSteamID()))
                    .setWeapon(getWeapons(woundsList, discordUser.getSteamID()))
                    .setMostKills(getMostKills(deathsList, discordUser.getSteamID()))
                    .setMostKilledBy(getMostKilledBy(deathsList, discordUser.getSteamID()))
                    .setMostRevives(getMostRevives(revivesList, discordUser.getSteamID()))
                    .setMostRevivedBy(getMOstRevivedBy(revivesList, discordUser.getSteamID()))
                    .setKd()
                    .setEffectiveness();
            return playerStats;
        } else {
            return null;
        }
    }

    private ArrayList<PlayerCount> getMOstRevivedBy(@NotNull List<Revives> revivesList, String steamID) {
        ArrayList<PlayerCount> playerCounts = new ArrayList<>();
        List<Revives> revivesByVictim = revivesList.stream()
                .filter(revives -> revives.getVictim() != null)
                .filter(revives -> revives.getVictim().equalsIgnoreCase(steamID))
                .toList();
        for (Revives revives : revivesByVictim) {
            String reviverName = revives.getReviverName();
            if (reviverName != null) {
                if (playerIsNotExist(playerCounts, reviverName)) {
                    playerCounts.add(new PlayerCount(reviverName));
                }
            }
        }
        playerCounts.sort((o1, o2) -> o2.getCount() - o1.getCount());
        return playerCounts;
    }

    private ArrayList<PlayerCount> getMostRevives(@NotNull List<Revives> revivesList, String steamID) {
        ArrayList<PlayerCount> playerCounts = new ArrayList<>();
        List<Revives> revivesByReviver = revivesList.stream()
                .filter(revives -> revives.getReviver() != null)
                .filter(revives -> revives.getReviver().equalsIgnoreCase(steamID))
                .toList();
        for (Revives revives : revivesByReviver) {
            String victimName = revives.getVictimName();
            if (victimName != null) {
                if (playerIsNotExist(playerCounts, victimName)) {
                    playerCounts.add(new PlayerCount(victimName));
                }
            }
        }
        playerCounts.sort((o1, o2) -> o2.getCount() - o1.getCount());
        return playerCounts;
    }

    private ArrayList<PlayerCount> getMostKilledBy(@NotNull List<Deaths> deathsList, String steamID) {
        ArrayList<PlayerCount> playerCounts = new ArrayList<>();
        List<Deaths> deathsByVictim = deathsList.stream()
                .filter(deaths -> deaths.getVictim() != null)
                .filter(deaths -> deaths.getVictim().equalsIgnoreCase(steamID))
                .filter(deaths -> {
                    if (deaths.getAttacker() != null) {
                        return !deaths.getAttacker().equalsIgnoreCase(deaths.getVictim());
                    }
                    return true;
                })
                .toList();
        for (Deaths deaths : deathsByVictim) {
            String attackerName = deaths.getAttackerName();
            if (attackerName != null) {
                if (playerIsNotExist(playerCounts, attackerName)) {
                    playerCounts.add(new PlayerCount(attackerName));
                }
            }
        }
        playerCounts.sort((o1, o2) -> o2.getCount() - o1.getCount());
        return playerCounts;
    }

    private ArrayList<PlayerCount> getMostKills(@NotNull List<Deaths> deathsList, String steamID) {
        ArrayList<PlayerCount> playerCounts = new ArrayList<>();
        List<Deaths> deathsByAttacker = deathsList.stream()
                .filter(deaths -> deaths.getAttacker() != null)
                .filter(deaths -> deaths.getAttacker().equalsIgnoreCase(steamID))
                .filter(deaths -> {
                    if (deaths.getVictim() != null) {
                        return !deaths.getVictim().equalsIgnoreCase(deaths.getAttacker());
                    }
                    return true;
                })
                .toList();
        for (Deaths deaths : deathsByAttacker) {
            String victimName = deaths.getVictimName();
            if (victimName != null) {
                if (playerIsNotExist(playerCounts, victimName)) {
                    playerCounts.add(new PlayerCount(victimName));
                }
            }
        }
        playerCounts.sort((o1, o2) -> o2.getCount() - o1.getCount());
        return playerCounts;
    }

    private boolean playerIsNotExist(@NotNull ArrayList<PlayerCount> playerCounts, String playerName) {
        for (PlayerCount playerCount : playerCounts) {
            if (playerCount.getPlayerName().equalsIgnoreCase(playerName)) {
                playerCount.addCount();
                return false;
            }
        }
        return true;
    }

    private @NotNull ArrayList<Gun> getWeapons(@NotNull List<Wounds> woundsList, String steamID) {
        ArrayList<Gun> guns = new ArrayList<>();
        List<Wounds> woundsListByAttacker = woundsList.stream()
                .filter(wounds -> wounds.getAttacker() != null)
                .filter(wounds -> wounds.getAttacker().equalsIgnoreCase(steamID))
                .toList();
        for (Wounds wounds : woundsListByAttacker) {
            String weapon = wounds.getWeapon();
            if (weapon != null) {
                if (!gunIsExist(guns, weapon)) {
                    Gun newGun = new Gun(weapon, 1);
                    guns.add(newGun);
                }
            }
        }
        guns.sort((o1, o2) -> o2.getCount() - o1.getCount());
        return guns;
    }

    private boolean gunIsExist(@NotNull ArrayList<Gun> guns, String weapon) {
        for (Gun gun : guns) {
            if (gun.getName().equalsIgnoreCase(weapon)) {
                gun.setCount(gun.getCount() + 1);
                return true;
            }
        }
        return false;
    }

    private int getTeamKills(@NotNull List<Wounds> woundsList, String steamID) {
        return woundsList.stream()
                .filter(wounds -> wounds.getAttacker() != null)
                .filter(wounds -> wounds.getAttacker().equalsIgnoreCase(steamID))
                .filter(wounds -> wounds.getTeamkill() != null)
                .filter(Wounds::getTeamkill)
                .toList().size();
    }

    private int getRevivesYou(@NotNull List<Revives> revivesList, String steamID) {
        return revivesList.stream()
                .filter(revives -> revives.getVictim() != null)
                .filter(revives -> revives.getVictim().equalsIgnoreCase(steamID))
                .toList().size();
    }

    private int getRevives(@NotNull List<Revives> revivesList, String steamID) {
        return revivesList.stream()
                .filter(revives -> revives.getReviver() != null)
                .filter(revives -> revives.getReviver().equalsIgnoreCase(steamID))
                .toList().size();
    }

    private int getWounds(@NotNull List<Wounds> woundsList, String steamID) {
        return woundsList.stream()
                .filter(wounds -> wounds.getAttacker() != null)
                .filter(wounds -> wounds.getAttacker().equalsIgnoreCase(steamID))
                .toList().size();
    }

    private int getDeaths(@NotNull List<Deaths> deathsList, @NotNull String steamID) {
        return deathsList.stream()
                .filter(deaths -> deaths.getVictim() != null)
                .filter(deaths -> deaths.getVictim().equalsIgnoreCase(steamID))
                .filter(deaths -> {
                    if (deaths.getAttacker() != null) {
                        return !deaths.getVictim().equalsIgnoreCase(deaths.getAttacker());
                    }
                    return true;
                })
                .toList().size();
    }

    private int getKills(@NotNull List<Deaths> deathsList, @NotNull String steamID) {
        return deathsList.stream()
                .filter(deaths -> deaths.getAttacker() != null)
                .filter(deaths -> deaths.getAttacker().equalsIgnoreCase(steamID))
                .filter(deaths -> {
                    if (deaths.getVictim() != null) {
                        return !deaths.getAttacker().equalsIgnoreCase(deaths.getVictim());
                    }
                    return true;
                })
                .toList().size();
    }

    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(STEAM_PROFILE.getName(), STEAM_PROFILE.getDescription())
                .addOption(OptionType.STRING, STEAM_PROFILE_64.getName(), STEAM_PROFILE_64.getDescription(), true));
        commandData.add(Commands.slash(STATS.getName(), STATS.getDescription()));
    }
}

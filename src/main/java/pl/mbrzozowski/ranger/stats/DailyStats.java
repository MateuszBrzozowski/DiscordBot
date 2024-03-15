package pl.mbrzozowski.ranger.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.stats.model.*;
import pl.mbrzozowski.ranger.stats.service.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class DailyStats {

    private final PlayerCountsService playerCountsService;
    private final ServerStatsService serverStatsService;
    private final RevivesService revivesService;
    private final PlayersService playersService;
    private final DeathsService deathsService;
    private final WoundsService woundsService;

    private LocalDateTime endTime;
    private LocalDateTime startTime;
    private final Set<String> uniquePlayers = new HashSet<>();
    private final Set<Integer> matches = new HashSet<>();
    private final List<PlayerStats> playerStats = new ArrayList<>();
    private final List<PlayerStats> playerStatsArtillery = new ArrayList<>();

    public int getPlayerCountNow() {
        Optional<PlayerCounts> last = playerCountsService.findLastWhereServer(1);
        if (last.isPresent()) {
            log.info("Players on server: {}", last.get().getPlayers());
            return last.get().getPlayers();
        }
        log.warn("Can not found last record in DB");
        return -1;
    }

    public void showEmbedOnStatsChannel() {
        TextChannel textChannel = RangersGuild.getTextChannel(RangersGuild.ChannelsId.STATS);
        if (textChannel == null) {
            log.warn("Null stats channel");
            return;
        }
        if (playerStats.size() == 0) {
            log.warn("No data to show");
            return;
        }
        textChannel.sendMessage("## Daily stats - Top 10\n" +
                Converter.LocalDateTimeToLongDateWWithShortTime(startTime) +
                " - " + Converter.LocalDateTimeToLongDateWWithShortTime(endTime) + "\n" +
                "Serwer: `✹↯✭ [PL/ENG] LTW #1 - Lead The Way polski serwer <RangersPL> | DiscordStatTrack`" +
                "Unique players: `" + uniquePlayers.size() + "`\n" +
                "Rounds: `" + matches.size() + "`").complete();
        textChannel.sendMessage("### \uD83D\uDDE1 Kills:\n```\n" + getMostKillsAsString() + "\n```\n" +
                "### ⚰ Deaths:\n```\n" + getMostDeathAsString() + "\n```\n" +
                "### ⚔ K/D:\n```\n" + getMostKDAsString() + "\n```").complete();
        textChannel.sendMessage("### ⚕ Revives - Top medic:\n```\n" + getMostRevivesAsString() + "\n```\n" +
                "### ⚕ Revived:\n```\n" + getMostRevivesYouAsString() + "\n```").complete();
        textChannel.sendMessage("### \uD83E\uDE78 Wounds:\n```\n" + getMostWoundsAsString() + "\n```\n" +
                "### \uD83D\uDEAB TeamKills:\n```\n" + getMostTeamKillsAsString() + "\n```").complete();
        if (playerStatsArtillery.size() != 0) {
            textChannel.sendMessage("### Damage - Artillery:\n```\n" + getMostArtilleryDamageAsString() + "\n```\n" +
                    "### \uD83E\uDE78 Wounds - Artillery:\n```\n" + getMostArtilleryWoundsAsString() + "\n```\n" +
                    "### \uD83D\uDDE1 Kills - Artillery:\n```\n" + getMostArtilleryKillsAsString() + "\n```").complete();
        }
    }

    @NotNull
    private String getMostArtilleryWoundsAsString() {
        playerStatsArtillery.sort((o1, o2) -> o2.getTeamKills() - o1.getTeamKills());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStatsArtillery.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStatsArtillery.get(i).getProfileName(), playerStatsArtillery.get(i).getTeamKills()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostArtilleryKillsAsString() {
        playerStatsArtillery.sort((o1, o2) -> o2.getKills() - o1.getKills());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStatsArtillery.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStatsArtillery.get(i).getProfileName(), playerStatsArtillery.get(i).getKills()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostArtilleryDamageAsString() {
        playerStatsArtillery.sort((o1, o2) -> o2.getWounds() - o1.getWounds());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStatsArtillery.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStatsArtillery.get(i).getProfileName(), playerStatsArtillery.get(i).getWounds()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostRevivesYouAsString() {
        playerStats.sort((o1, o2) -> o2.getRevivesYou() - o1.getRevivesYou());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStats.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStats.get(i).getProfileName(), playerStats.get(i).getRevivesYou()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostRevivesAsString() {
        playerStats.sort((o1, o2) -> o2.getRevives() - o1.getRevives());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStats.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStats.get(i).getProfileName(), playerStats.get(i).getRevives()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostTeamKillsAsString() {
        playerStats.sort((o1, o2) -> o2.getTeamKills() - o1.getTeamKills());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStats.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStats.get(i).getProfileName(), playerStats.get(i).getTeamKills()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostWoundsAsString() {
        playerStats.sort((o1, o2) -> o2.getWounds() - o1.getWounds());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStats.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStats.get(i).getProfileName(), playerStats.get(i).getWounds()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostKDAsString() {
        playerStats.sort(Comparator.comparingDouble(PlayerStats::getKd));
        Collections.reverse(playerStats);
        StringBuilder builder = new StringBuilder();
        int j = 1;
        for (int i = 0; i < playerStats.size() && j <= 10; i++) {
            if (playerStats.get(i).getKills() != 0 && playerStats.get(i).getDeaths() != 0) {
                builder.append(String.format("%-2d- %-30s %-2.2f", j,
                        playerStats.get(i).getProfileName(),
                        playerStats.get(i).getKd()));
                builder.append("\n");
                j++;
            }
        }
        return builder.toString();
    }

    @NotNull
    private String getMostDeathAsString() {
        playerStats.sort((o1, o2) -> o2.getDeaths() - o1.getDeaths());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStats.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStats.get(i).getProfileName(), playerStats.get(i).getDeaths()));
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    private String getMostKillsAsString() {
        playerStats.sort((o1, o2) -> o2.getKills() - o1.getKills());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerStats.size() && i < 10; i++) {
            builder.append(String.format("%-2d- %-30s %s", i + 1, playerStats.get(i).getProfileName(), playerStats.get(i).getKills()));
            builder.append("\n");
        }
        return builder.toString();
    }

    public void pullData() {
        List<PlayerCounts> activePeriod = getActivePeriod();
        if (activePeriod.isEmpty()) {
            log.info("Server empty. No data to check");
            return;
        }
        endTime = activePeriod.get(0).getTime();
        startTime = activePeriod.get(activePeriod.size() - 1).getTime();
        List<Deaths> deaths = deathsService.findByTimeBetweenWhereServer(startTime, endTime, 1);
        List<Revives> revives = revivesService.findByTimeBetweenWhereServer(startTime, endTime, 1);
        List<Wounds> wounds = woundsService.findByTimeBetweenWhereServer(startTime, endTime, 1);
        getUniquePlayersCount(deaths);
        getMatches(deaths);
        getArtilleryList(wounds, deaths);
        setStatsAllPlayers(deaths, revives, wounds);
    }

    private void getArtilleryList(@NotNull List<Wounds> wounds, List<Deaths> deaths) {
        Set<String> steamIds = new HashSet<>();
        for (Wounds wound : wounds) {
            if (wound.getAttacker() != null && wound.getWeapon() != null && wound.getWeapon().contains("155mm")) {
                steamIds.add(wound.getAttacker());
            }
        }
        List<Wounds> projectileWounds = wounds.stream().filter(w -> w.getWeapon().contains("155mm")).toList();
        List<Deaths> projectileDeaths = deaths.stream().filter(w -> w.getWeapon().contains("155mm")).toList();
        for (String steamId : steamIds) {
            Optional<Players> playerOptional = playersService.findBySteamId(steamId);
            if (playerOptional.isEmpty()) {
                continue;
            }
            PlayerStats player = PlayerStats.builder()
                    .profileName(playerOptional.get().getLastName())
                    .kills(serverStatsService.getKills(projectileDeaths, steamId))
                    .wounds(getDamage(projectileWounds, steamId))
                    .teamKills(serverStatsService.getWounds(projectileWounds, steamId))
                    .build();
            playerStatsArtillery.add(player);
        }

    }

    private int getDamage(@NotNull List<Wounds> wounds, String steamId) {
        List<Wounds> woundsList = wounds.stream()
                .filter(wound -> wound.getAttacker() != null)
                .filter(wound -> wound.getAttacker().equalsIgnoreCase(steamId))
                .toList();
        float damage = 0;
        for (Wounds wound : woundsList) {
            damage += wound.getDamage();
        }
        return (int) damage;
    }

    private void setStatsAllPlayers(List<Deaths> deaths, List<Revives> revives, List<Wounds> wounds) {
        for (String steamId : uniquePlayers) {
            Optional<Players> playerOptional = playersService.findBySteamId(steamId);
            if (playerOptional.isEmpty()) {
                continue;
            }
            PlayerStats player = PlayerStats.builder()
                    .profileName(playerOptional.get().getLastName())
                    .kills(serverStatsService.getKills(deaths, steamId))
                    .deaths(serverStatsService.getDeaths(deaths, steamId))
                    .wounds(serverStatsService.getWounds(wounds, steamId))
                    .revives(serverStatsService.getRevives(revives, steamId))
                    .revivesYou(serverStatsService.getRevivesYou(revives, steamId))
                    .teamKills(serverStatsService.getTeamKills(wounds, steamId))
                    .build();
            playerStats.add(player);
        }
    }

    private void getMatches(@NotNull List<Deaths> deaths) {
        for (Deaths death : deaths) {
            matches.add(death.getMatch());
        }
    }

    private void getUniquePlayersCount(@NotNull List<Deaths> deaths) {
        for (Deaths death : deaths) {
            if (death.getAttacker() != null) {
                uniquePlayers.add(death.getAttacker());
            }
            if (death.getVictim() != null) {
                uniquePlayers.add(death.getVictim());
            }
        }
    }

    @NotNull
    private List<PlayerCounts> getActivePeriod() {
        List<PlayerCounts> lastTwoDays = playerCountsService.findLastTwoDaysWhereServer(1);
        lastTwoDays.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        int firstIndex = 0;
        for (int i = 0; i < lastTwoDays.size(); i++) {
            if (lastTwoDays.get(i).getPlayers() > 10) {
                firstIndex = i;
                break;
            }
        }
        int lastIndex = firstIndex;
        boolean wasFull = false;
        for (int i = firstIndex; i < lastTwoDays.size(); i++) {
            if (lastTwoDays.get(i).getPlayers() > 80) {
                wasFull = true;
            }
            if (wasFull) {
                if (lastTwoDays.get(i).getPlayers() < 15) {
                    lastIndex = i;
                    break;
                }
            }
        }
        if (lastIndex == firstIndex) {
            return new ArrayList<>();
        }
        List<PlayerCounts> lastActiveServer = new ArrayList<>();
        for (int i = firstIndex; i <= lastIndex; i++) {
            lastActiveServer.add(lastTwoDays.get(i));
        }
        return lastActiveServer;
    }
}

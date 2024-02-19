package pl.mbrzozowski.ranger.stats;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
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
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;
import pl.mbrzozowski.ranger.stats.model.*;
import pl.mbrzozowski.ranger.stats.service.*;

import java.awt.*;
import java.sql.SQLSyntaxErrorException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.guild.SlashCommands.*;

@Service
@Slf4j
public class ServerStatsService implements SlashCommand {

    private final PlayerCountsService playerCountsService;
    private final DiscordUserService discordUserService;
    private final SettingsService settingsService;
    private final PlayersService playersService;
    private final RevivesService revivesService;
    private final DeathsService deathsService;
    private final WoundsService woundsService;
    private LocalDateTime dateTime;

    public ServerStatsService(PlayerCountsService playerCountsService,
                              DeathsService deathsService,
                              DiscordUserService discordUserService,
                              SettingsService settingsService,
                              RevivesService revivesService,
                              PlayersService playersService,
                              WoundsService woundsService) {
        this.playerCountsService = playerCountsService;
        this.discordUserService = discordUserService;
        this.settingsService = settingsService;
        this.revivesService = revivesService;
        this.playersService = playersService;
        this.deathsService = deathsService;
        this.woundsService = woundsService;
        pullDateTime();
    }

    private void pullDateTime() {
        Optional<String> optional = settingsService.find(SettingsKey.STATS_DATE_FROM);
        if (optional.isPresent()) {
            try {
                dateTime = LocalDateTime.parse(optional.get());
            } catch (Exception e) {
                settingsService.deleteByKey(SettingsKey.STATS_DATE_FROM);
            }

        }
    }

    public void runAutoStatsAfterDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(LocalDateTime.now().getYear(),
                LocalDateTime.now().getMonthValue() - 1,
                LocalDateTime.now().getDayOfMonth(),
                LocalDateTime.now().getHour(),
                LocalDateTime.now().getMinute(),
                0);
        Timer timer = new Timer();
        AutoCheckStatsAfterDay autoCheckStatsAfterDay = new AutoCheckStatsAfterDay(this);
        timer.scheduleAtFixedRate(autoCheckStatsAfterDay, calendar.getTime(), 10 * 60 * 1000);
        log.info("Auto stats after day active");
    }


    public void autoStatsAfterDay() {
        StatsAfterDay statsAfterDay = new StatsAfterDay(playerCountsService, revivesService, playersService, deathsService, woundsService);
        int playerCount = statsAfterDay.getPlayerCountNow();
        if (playerCount > 0) {
            return;
        }

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
                ResponseMessage.playerStatsIsNull(event);
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
        builder.setDescription("## **Profile info:**\n```yaml\n" + playerStats.getProfileName() + "\n```");
        builder.addField("⚔ K/D", "**" + df.format(playerStats.getKd()) + "**", true);
        builder.addField("⚔ K/D " + LocalDateTime.now().withYear(LocalDateTime.now().getYear() - 1).getYear() + "r.",
                "**" + (playerStats.getKdLastYear() > 0.01 ? df.format(playerStats.getKdLastYear()) : "-") + "**", true);
        builder.addField("⚔ K/D " + LocalDateTime.now().getYear() + "r.",
                "**" + (playerStats.getKdCurrentYear() > 0.01 ? df.format(playerStats.getKdCurrentYear()) : "-") + "**", true);
        builder.addField("⚔ K/D last 7 days",
                "**" + (playerStats.getKdLast7Days() > 0.01 ? df.format(playerStats.getKdLast7Days()) : "-") + "**", true);
        builder.addField("⚔ K/D last 30 days",
                "**" + (playerStats.getKdLast30Days() > 0.01 ? df.format(playerStats.getKdLast30Days()) : "-") + "**", true);
        builder.addField("⚔ K/D last 90 days",
                "**" + (playerStats.getKdLast90Days() > 0.01 ? df.format(playerStats.getKdLast90Days()) : "-") + "**", true);
        builder.addBlankField(false);
        builder.addField("\uD83D\uDDE1 Kills", "**" + playerStats.getKills() + "** kill(s)", true);
        builder.addField("⚰ Deaths", "**" + playerStats.getDeaths() + "** death(s)", true);
        builder.addField("\uD83E\uDE78 Wounds", "**" + playerStats.getWounds() + "** wound(s)", true);
        builder.addField("⚔ Kills/Wounds", "**" + df.format(playerStats.getEffectiveness()) + "** effectiveness", true);
        builder.addField("⚕ Revives", "**" + playerStats.getRevives() + "** revive(s)", true);
        builder.addField("⚕ Revived", "**" + playerStats.getRevivesYou() + "** revive(s)", true);
        builder.addField("\uD83D\uDC9E Weapon", playerStats.getWeapon(), true);
        builder.addField("\uD83D\uDEAB TeamKills", "**" + playerStats.getTeamKills() + "** teamkill(s)", true);
        builder.addBlankField(true);
        builder.addBlankField(false);
        builder.addField("Most kills", playerStats.getMostKills(), true);
        builder.addField("Most killed by", playerStats.getMostKilledBy(), true);
        builder.addField("Most revives", playerStats.getMostRevives(), true);
        builder.addField("Most revived by", playerStats.getMostRevivedBy(), true);
        builder.setFooter("Data from " + getDate());
        event.getHook().editOriginal("<@" + userID + ">").setEmbeds(builder.build()).queue();
        log.info("Embed with stats sent for user(id=" + userID + ")");
    }

    @NotNull
    private String getDate() {
        return dateTime.getDayOfMonth() + "." + String.format("%02d", dateTime.getMonthValue()) + "." + dateTime.getYear() + "r.";
    }

    private @Nullable PlayerStats pullStatsFromDB(String userId) {
        Optional<DiscordUser> userOptional = discordUserService.findByUserId(userId);
        if (userOptional.isPresent()) {
            DiscordUser discordUser = userOptional.get();
            Optional<Players> steamUsersOptional = playersService.findBySteamId(discordUser.getSteamID());
            if (steamUsersOptional.isEmpty()) {
                return null;
            }
            List<Deaths> deathsList = getDeaths(discordUser);
            List<Revives> revivesList = getRevives(discordUser);
            List<Wounds> woundsList = getWounds(discordUser);
            if (dateTime == null && deathsList.size() > 0) {
                dateTime = deathsList.get(0).getTime();
            }
            return PlayerStats.builder()
                    .profileName(steamUsersOptional.get().getLastName())
                    .kills(getKills(deathsList, discordUser.getSteamID()))
                    .deaths(getDeaths(deathsList, discordUser.getSteamID()))
                    .wounds(getWounds(woundsList, discordUser.getSteamID()))
                    .revives(getRevives(revivesList, discordUser.getSteamID()))
                    .revivesYou(getRevivesYou(revivesList, discordUser.getSteamID()))
                    .teamKills(getTeamKills(woundsList, discordUser.getSteamID()))
                    .weapon(getWeapons(woundsList, discordUser.getSteamID()))
                    .mostKills(getMostKills(deathsList, discordUser.getSteamID()))
                    .mostKilledBy(getMostKilledBy(deathsList, discordUser.getSteamID()))
                    .mostRevives(getMostRevives(revivesList, discordUser.getSteamID()))
                    .mostRevivedBy(getMOstRevivedBy(revivesList, discordUser.getSteamID()))
                    .kdLast7Days(getKDLast7Days(deathsList, discordUser.getSteamID()))
                    .kdLast30Days(getKDLast30Days(deathsList, discordUser.getSteamID()))
                    .kdLast90Days(getKDLast90Days(deathsList, discordUser.getSteamID()))
                    .kdLastYear(getKDLastYear(deathsList, discordUser.getSteamID()))
                    .kdCurrentYear(getKDCurrentYear(deathsList, discordUser.getSteamID()))
                    .build();
        } else {
            return null;
        }
    }

    @NotNull
    private List<Wounds> getWounds(@NotNull DiscordUser discordUser) {
        List<Wounds> woundsList;
        if (dateTime == null) {
            woundsList = woundsService.findByAttackerOrVictim(discordUser.getSteamID(), discordUser.getSteamID());
        } else {
            woundsList = woundsService.findByAttackerOrVictimAndTimeAfter(discordUser.getSteamID(), dateTime);
        }
        return woundsList;
    }

    @NotNull
    private List<Revives> getRevives(@NotNull DiscordUser discordUser) {
        List<Revives> revivesList;
        if (dateTime == null) {
            revivesList = revivesService.findByReviverOrVictim(discordUser.getSteamID(), discordUser.getSteamID());
        } else {
            revivesList = revivesService.findByReviverOrVictimAndTimeAfter(discordUser.getSteamID(), dateTime);
        }
        return revivesList;
    }

    @NotNull
    private List<Deaths> getDeaths(@NotNull DiscordUser discordUser) {
        List<Deaths> deathsList;
        if (dateTime == null) {
            deathsList = deathsService.findByAttackerOrVictim(discordUser.getSteamID(), discordUser.getSteamID());
        } else {
            deathsList = deathsService.findByAttackerOrVictimAndTimeAfter(discordUser.getSteamID(), dateTime);
        }
        return deathsList;
    }

    @NotNull
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

    @NotNull
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

    @NotNull
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

    @NotNull
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
                .filter(deaths -> deaths.getAttacker().equals(steamID))
                .filter(deaths -> {
                    if (deaths.getVictim() != null) {
                        return !deaths.getAttacker().equals(deaths.getVictim());
                    }
                    return true;
                })
                .toList().size();
    }

    private double getKDLastYear(List<Deaths> deathsList, String steamID) {
        return getKDFromYear(deathsList, steamID, LocalDateTime.now().withYear(LocalDateTime.now().getYear() - 1).getYear());
    }

    private double getKDCurrentYear(List<Deaths> deathsList, String steamID) {
        return getKDFromYear(deathsList, steamID, LocalDateTime.now().getYear());
    }

    private double getKDFromYear(@NotNull List<Deaths> deathsList, String steamID, int year) {
        double kills = deathsList.stream()
                .filter(deaths -> deaths.getAttacker() != null)
                .filter(deaths -> deaths.getAttacker().equals(steamID))
                .filter(deaths -> {
                    if (deaths.getVictim() != null) {
                        return !deaths.getAttacker().equals(deaths.getVictim());
                    }
                    return true;
                })
                .filter(deaths -> deaths.getTime().isAfter(LocalDateTime.now(ZoneOffset.UTC).withDayOfYear(LocalDateTime.MIN.getDayOfYear()).withYear(year)))
                .filter(deaths -> deaths.getTime().isBefore(LocalDateTime.now(ZoneOffset.UTC).withDayOfYear(LocalDateTime.MAX.getDayOfYear()).withYear(year)))
                .toList().size();
        double death = deathsList.stream()
                .filter(deaths -> deaths.getVictim() != null)
                .filter(deaths -> deaths.getVictim().equalsIgnoreCase(steamID))
                .filter(deaths -> {
                    if (deaths.getAttacker() != null) {
                        return !deaths.getVictim().equalsIgnoreCase(deaths.getAttacker());
                    }
                    return true;
                })
                .filter(deaths -> deaths.getTime().isAfter(LocalDateTime.now(ZoneOffset.UTC).withDayOfYear(LocalDateTime.MIN.getDayOfYear()).withYear(year)))
                .filter(deaths -> deaths.getTime().isBefore(LocalDateTime.now(ZoneOffset.UTC).withDayOfYear(LocalDateTime.MAX.getDayOfYear()).withYear(year)))
                .toList().size();
        if (death != 0) {
            return kills / death;
        }
        return 0;
    }

    private double getKDLast7Days(List<Deaths> deathsList, String steamID) {
        return getKDLastDays(deathsList, steamID, 7);
    }

    private double getKDLast30Days(List<Deaths> deathsList, String steamID) {
        return getKDLastDays(deathsList, steamID, 30);
    }

    private double getKDLast90Days(List<Deaths> deathsList, String steamID) {
        return getKDLastDays(deathsList, steamID, 90);
    }

    private double getKDLastDays(@NotNull List<Deaths> deathsList, String steamID, int days) {
        double kills = deathsList.stream()
                .filter(deaths -> deaths.getAttacker() != null)
                .filter(deaths -> deaths.getAttacker().equals(steamID))
                .filter(deaths -> {
                    if (deaths.getVictim() != null) {
                        return !deaths.getAttacker().equals(deaths.getVictim());
                    }
                    return true;
                })
                .filter(deaths -> deaths.getTime().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusDays(days)))
                .toList().size();
        double death = deathsList.stream()
                .filter(deaths -> deaths.getVictim() != null)
                .filter(deaths -> deaths.getVictim().equalsIgnoreCase(steamID))
                .filter(deaths -> {
                    if (deaths.getAttacker() != null) {
                        return !deaths.getVictim().equalsIgnoreCase(deaths.getAttacker());
                    }
                    return true;
                })
                .filter(deaths -> deaths.getTime().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusDays(days)))
                .toList().size();
        if (death != 0) {
            return kills / death;
        }
        return 0;
    }


    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(STEAM_PROFILE.getName(), STEAM_PROFILE.getDescription())
                .addOption(OptionType.STRING, STEAM_PROFILE_64.getName(), STEAM_PROFILE_64.getDescription(), true));
        commandData.add(Commands.slash(STATS.getName(), STATS.getDescription()));
        commandData.add(Commands.slash(STATS_DATE.getName(), STATS_DATE.getDescription())
                .addOption(OptionType.INTEGER, "day", "Dzień", true)
                .addOption(OptionType.INTEGER, "month", "Miesiąc", true)
                .addOption(OptionType.INTEGER, "year", "Rok", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }

    public void setDate(@NotNull SlashCommandInteractionEvent event) {
        int day = Objects.requireNonNull(event.getOption("day")).getAsInt();
        int month = Objects.requireNonNull(event.getOption("month")).getAsInt();
        int year = Objects.requireNonNull(event.getOption("year")).getAsInt();
        try {
            dateTime = LocalDateTime.now().withDayOfMonth(day).withMonth(month).withYear(year).withHour(0).withMinute(1);
            if (dateTime.isAfter(LocalDateTime.now().minusDays(7))) {
                event.reply("Minimum 7 dni!").setEphemeral(true).queue();
                return;
            }
            event.reply("Ustawiona data: " + dateTime.getDayOfMonth() + "." +
                            String.format("%02d", dateTime.getMonthValue()) + "." + dateTime.getYear())
                    .setEphemeral(true)
                    .queue();
            settingsService.save(SettingsKey.STATS_DATE_FROM, dateTime.toString());
        } catch (Exception e) {
            event.reply("Data nieprawidłowa!").setEphemeral(true).queue();
        }
    }
}

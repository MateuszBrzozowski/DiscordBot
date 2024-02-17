package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.games.*;
import pl.mbrzozowski.ranger.games.birthday.BirthdayService;
import pl.mbrzozowski.ranger.games.essa.Essa;
import pl.mbrzozowski.ranger.games.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.games.reputation.ReputationService;
import pl.mbrzozowski.ranger.games.timeout.RandomTimeout;
import pl.mbrzozowski.ranger.members.clan.rank.RankService;
import pl.mbrzozowski.ranger.model.ImplCleaner;
import pl.mbrzozowski.ranger.recruit.RecruitBlackListService;
import pl.mbrzozowski.ranger.role.RoleService;
import pl.mbrzozowski.ranger.server.seed.call.SeedCallService;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;
import pl.mbrzozowski.ranger.stats.ServerStatsService;

import static pl.mbrzozowski.ranger.guild.SlashCommands.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final RecruitBlackListService recruitBlackListService;
    private final EventsGeneratorService eventsGeneratorService;
    private final EventsSettingsService eventsSettingsService;
    private final ServerStatsService serverStatsService;
    private final ReputationService reputationService;
    private final SettingsService settingsService;
    private final GiveawayService giveawayService;
    private final SeedCallService seedCallService;
    private final BirthdayService birthdayService;
    private final EventService eventService;
    private final RoleService roleService;
    private final ImplCleaner implCleaner;
    private final RankService rankService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.info(event.getUser() + " - used slash command(command={})", event.getName());
        String name = event.getName();
        if (name.equals(ADD_ROLE_TO_RANGER.getName())) {
            roleService.addRole(event);
        } else if (name.equals(REMOVE_ROLE_FROM_RANGER.getName())) {
            roleService.removeRole(event);
        } else if (name.equals(ROLE.getName())) {
            roleService.roleEvent(event);
        } else if (name.equals(STEAM_PROFILE.getName())) {
            serverStatsService.profile(event);
        } else if (name.equals(STATS.getName())) {
            serverStatsService.stats(event);
        } else if (name.equals(STATS_DATE.getName())) {
            serverStatsService.setDate(event);
        } else if (name.equals(DICE.getName())) {
            new Dice().start(event);
        } else if (name.equals(COIN.getName())) {
            new Coin().start(event);
        } else if (name.equals(ESSA.getName())) {
            Essa.getInstance().start(event);
        } else if (name.equals(REP.getName())) {
            reputationService.show(event);
        } else if (name.equals(TOP_REP.getName())) {
            reputationService.showTopTen(event);
        } else if (name.equals(RANDOM_TIMEOUT.getName())) {
            RandomTimeout.getInstance().start(event);
        } else if (name.equals(RANGER_OF_THE_DAY.getName())) {
            new RangerOfTheDay(settingsService).start(event);
        } else if (name.equals(BIRTHDAY.getName())) {
            birthdayService.start(event);
        } else if (name.equals(BIRTHDAY_SET.getName())) {
            birthdayService.setDate(event, false);
        } else if (name.equals(BIRTHDAY_ADMIN_SET.getName())) {
            birthdayService.setDate(event, true);
        } else if (name.equals(GIVEAWAY_CREATE.getName())) {
            giveawayService.create(event);
        } else if (name.equals(GIVEAWAY_END.getName())) {
            giveawayService.end(event);
        } else if (name.equals(GIVEAWAY_CANCEL.getName())) {
            giveawayService.cancel(event);
        } else if (name.equals(GIVEAWAY_LIST.getName())) {
            giveawayService.showActive(event);
        } else if (name.equals(GIVEAWAY_RE_ROLL.getName())) {
            giveawayService.reRoll(event);
        } else if (name.equals(FIX_EVENT_EMBED.getName())) {
            eventService.fixEmbed(event);
        } else if (name.equals(FIX_GIVEAWAY_EMBED.getName())) {
            giveawayService.fixEmbed(event);
        } else if (name.equals(EVENT_CREATE.getName())) {
            eventsGeneratorService.createGenerator(event, eventService);
        } else if (name.equals(EVENT_SETTINGS.getName())) {
            eventsSettingsService.createSettings(event);
        } else if (name.equals(RECRUIT_BLACK_LIST_ADD.getName())) {
            recruitBlackListService.addToList(event);
        } else if (name.equals(RECRUIT_BLACK_LIST_REMOVE.getName())) {
            recruitBlackListService.removeFromList(event);
        } else if (name.equals(RECRUIT_BLACK_LIST_INFO.getName())) {
            recruitBlackListService.infoAboutUser(event);
        } else if (name.equals(RECRUIT_DELETE_CHANNEL_DELAY.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.RECRUIT_DELETE_CHANNEL_DELAY);
        } else if (name.equals(EVENT_DELETE_CHANNEL_DELAY.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.EVENT_DELETE_CHANNEL_DELAY);
        } else if (name.equals(EVENT_DELETE_CHANNEL_TACTICAL_DELAY.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.EVENT_DELETE_CHANNEL_TACTICAL_DELAY);
        } else if (name.equals(SERVER_SERVICE_DELETE_CHANNEL.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.SERVER_SERVICE_DELETE_CHANNEL);
        } else if (name.equals(SERVER_SERVICE_CLOSE_CHANNEL.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.SERVER_SERVICE_CLOSE_CHANNEL);
        } else if (name.equals(RANK_ROLE_ADD.getName())) {
            rankService.addRole(event);
        } else if (name.equals(RANK_ROLE_FIND_BY_NAME.getName())) {
            rankService.findByName(event);
        } else if (name.equals(RANK_ROLE_FIND_BY_DISCORD_ID.getName())) {
            rankService.findByDiscordId(event);
        } else if (name.equals(RANK_ROLE_REMOVE.getName())) {
            rankService.deleteByDiscordId(event);
        } else if (name.equals(SEED_CALL_AMOUNT.getName())) {
            seedCallService.setMaxAmount(event);
        } else if (name.equals(SEED_CALL_ENABLE.getName())) {
            seedCallService.switchOnOff(event);
        } else if (name.equals(SEED_CALL_CONDITIONS.getName())) {
            seedCallService.addConditions(event);
        } else if (name.equals(SEED_CALL_CONDITIONS_REMOVE.getName())) {
            seedCallService.removeConditions(event);
        } else if (name.equals(SEED_CALL_CONDITIONS_INFO.getName())) {
            seedCallService.conditionsInfo(event);
        } else if (name.equals(SEED_CALL_MESSAGE.getName())) {
            seedCallService.addMessage(event);
        } else if (name.equals(SEED_CALL_MESSAGE_REMOVE.getName())) {
            seedCallService.removeMessage(event);
        } else if (name.equals(SEED_CALL_MESSAGE_INFO.getName())) {
            seedCallService.messagesInfo(event);
        } else if (name.equals(SEED_CALL_ROLE_ADD.getName())) {
            seedCallService.setRole(event);
        } else if (name.equals(SEED_CALL_ROLE_REMOVE.getName())) {
            seedCallService.deleteRole(event);
        } else if (name.equals(SEED_CALL_CHANNEL.getName())) {
            seedCallService.setChannel(event);
        } else if (name.equals(SEED_CALL_REPLACEMENT_LEVELS.getName())) {
            seedCallService.replacementLevels(event);
        }
    }
}

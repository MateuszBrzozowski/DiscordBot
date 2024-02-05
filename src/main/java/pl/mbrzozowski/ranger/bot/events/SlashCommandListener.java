package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.games.Coin;
import pl.mbrzozowski.ranger.games.Dice;
import pl.mbrzozowski.ranger.games.Essa;
import pl.mbrzozowski.ranger.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.model.ImplCleaner;
import pl.mbrzozowski.ranger.recruit.RecruitBlackListService;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.role.RoleService;
import pl.mbrzozowski.ranger.server.service.ServerService;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.stats.ServerStats;

import java.util.ArrayList;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final RecruitBlackListService recruitBlackListService;
    private final EventsSettingsService eventsSettingsService;
    private final EventsGeneratorService eventsGeneratorService;
    private final RecruitsService recruitsService;
    private final GiveawayService giveawayService;
    private final ServerService serverService;
    private final EventService eventService;
    private final RoleService roleService;
    private final ServerStats serverStats;
    private final ImplCleaner implCleaner;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.info(event.getUser() + " - used slash command(command={})", event.getName());
        String name = event.getName();
        if (name.equalsIgnoreCase(ADD_ROLE_TO_RANGER.getName())) {
            roleService.addRole(event);
        } else if (name.equalsIgnoreCase(REMOVE_ROLE_FROM_RANGER.getName())) {
            roleService.removeRole(event);
        } else if (name.equals(ROLE.getName())) {
            roleService.roleEvent(event);
        } else if (name.equalsIgnoreCase(STEAM_PROFILE.getName())) {
            serverStats.profile(event);
        } else if (name.equalsIgnoreCase(STATS.getName())) {
            serverStats.stats(event);
        } else if (name.equalsIgnoreCase(DICE.getName())) {
            Dice.start(event);
        } else if (name.equalsIgnoreCase(COIN.getName())) {
            Coin.start(event);
        } else if (name.equalsIgnoreCase(ESSA.getName())) {
            Essa.start(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_CREATE.getName())) {
            giveawayService.create(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_END.getName())) {
            giveawayService.end(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_CANCEL.getName())) {
            giveawayService.cancel(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_LIST.getName())) {
            giveawayService.showActive(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_RE_ROLL.getName())) {
            giveawayService.reRoll(event);
        } else if (name.equalsIgnoreCase(FIX_EVENT_EMBED.getName())) {
            eventService.fixEmbed(event);
        } else if (name.equalsIgnoreCase(FIX_GIVEAWAY_EMBED.getName())) {
            giveawayService.fixEmbed(event);
        } else if (name.equalsIgnoreCase(EVENT_CREATE.getName())) {
            eventsGeneratorService.createGenerator(event, eventService);
        } else if (name.equalsIgnoreCase(EVENT_SETTINGS.getName())) {
            eventsSettingsService.createSettings(event);
        } else if (name.equalsIgnoreCase(RECRUIT_BLACK_LIST_ADD.getName())) {
            recruitBlackListService.addToList(event);
        } else if (name.equalsIgnoreCase(RECRUIT_BLACK_LIST_REMOVE.getName())) {
            recruitBlackListService.removeFromList(event);
        } else if (name.equalsIgnoreCase(RECRUIT_BLACK_LIST_INFO.getName())) {
            recruitBlackListService.infoAboutUser(event);
        } else if (name.equalsIgnoreCase(RECRUIT_DELETE_CHANNEL_DELAY.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.RECRUIT_DELETE_CHANNEL_DELAY);
        } else if (name.equalsIgnoreCase(EVENT_DELETE_CHANNEL_DELAY.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.EVENT_DELETE_CHANNEL_DELAY);
        } else if (name.equalsIgnoreCase(EVENT_DELETE_CHANNEL_TACTICAL_DELAY.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.EVENT_DELETE_CHANNEL_TACTICAL_DELAY);
        } else if (name.equalsIgnoreCase(SERVER_SERVICE_DELETE_CHANNEL.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.SERVER_SERVICE_DELETE_CHANNEL);
        } else if (name.equalsIgnoreCase(SERVER_SERVICE_CLOSE_CHANNEL.getName())) {
            implCleaner.setDelayToDeleteChannel(event, SettingsKey.SERVER_SERVICE_CLOSE_CHANNEL);
        }
    }

    public void getCommandsData(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(DICE.getName(), DICE.getDescription()));
        commandData.add(Commands.slash(COIN.getName(), COIN.getDescription()));
        commandData.add(Commands.slash(ESSA.getName(), ESSA.getDescription()));
    }
}

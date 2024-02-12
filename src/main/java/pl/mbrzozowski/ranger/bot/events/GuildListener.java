package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.games.Coin;
import pl.mbrzozowski.ranger.games.Dice;
import pl.mbrzozowski.ranger.games.Essa;
import pl.mbrzozowski.ranger.games.RandomTimeout;
import pl.mbrzozowski.ranger.games.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.games.reputation.ReputationService;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.members.clan.rank.RankService;
import pl.mbrzozowski.ranger.model.ImplCleaner;
import pl.mbrzozowski.ranger.recruit.RecruitBlackListService;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.role.RoleService;
import pl.mbrzozowski.ranger.server.seed.call.SeedCallService;
import pl.mbrzozowski.ranger.server.service.ServerService;
import pl.mbrzozowski.ranger.stats.ServerStatsService;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GuildListener extends ListenerAdapter {

    private final RecruitBlackListService recruitBlackListService;
    private final ServerStatsService serverStatsService;
    private final ReputationService reputationService;
    private final RecruitsService recruitsService;
    private final GiveawayService giveawayService;
    private final SeedCallService seedCallService;
    private final ServerService serverService;
    private final EventService eventService;
    private final RoleService roleService;
    private final ImplCleaner implCleaner;
    private final RankService rankService;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().equals(RangersGuild.getGuild())) {
            ArrayList<CommandData> commandData = new ArrayList<>();
            getCommandList(commandData);
            recruitsService.cleanDB(event);
            implCleaner.autoDeleteChannels();
            implCleaner.autoCloseChannel();
            seedCallService.run();
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }

    private void getCommandList(ArrayList<CommandData> commandData) {
        getSlashCommands(commandData);
        getContextMenu(commandData);
    }

    private void getSlashCommands(ArrayList<CommandData> commandData) {
        recruitBlackListService.getSlashCommandsList(commandData);
        serverStatsService.getSlashCommandsList(commandData);
        reputationService.getSlashCommandsList(commandData);
        recruitsService.getSlashCommandsList(commandData);
        giveawayService.getSlashCommandsList(commandData);
        seedCallService.getSlashCommandsList(commandData);
        serverService.getSlashCommandsList(commandData);
        eventService.getSlashCommandsList(commandData);
        roleService.getSlashCommandsList(commandData);
        rankService.getSlashCommandsList(commandData);
        Essa.getInstance().getSlashCommandsList(commandData);
        new Dice().getSlashCommandsList(commandData);
        new Coin().getSlashCommandsList(commandData);
        new RandomTimeout().getSlashCommandsList(commandData);
    }

    private void getContextMenu(@NotNull ArrayList<CommandData> commandData) {
        reputationService.getContextCommandsList(commandData);
    }

}

package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.games.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.guild.ContextCommands;
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
    private final SlashCommandListener slashCommandListener;
    private final ServerStatsService serverStatsService;
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
            getContextMenu(commandData);
            recruitsService.cleanDB(event);
            implCleaner.autoDeleteChannels();
            implCleaner.autoCloseChannel();
            seedCallService.run();
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }

    private void getContextMenu(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.context(Command.Type.USER, ContextCommands.REPUTATION.getName()));
        commandData.add(Commands.message(ContextCommands.REPUTATION.getName()));
    }

    private void getCommandList(ArrayList<CommandData> commandData) {
        recruitBlackListService.getCommandsList(commandData);
        slashCommandListener.getCommandsData(commandData);
        serverStatsService.getCommandsList(commandData);
        recruitsService.getCommandsList(commandData);
        giveawayService.getCommandsList(commandData);
        seedCallService.getCommandsList(commandData);
        serverService.getCommandsList(commandData);
        eventService.getCommandsList(commandData);
        roleService.getCommandsList(commandData);
        rankService.getCommandsList(commandData);
    }

}

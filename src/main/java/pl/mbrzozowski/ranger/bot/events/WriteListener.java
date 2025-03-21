package pl.mbrzozowski.ranger.bot.events;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.bot.events.writing.*;
import pl.mbrzozowski.ranger.configuration.content.ContentService;
import pl.mbrzozowski.ranger.disboard.DisboardService;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.members.GuildMembersService;
import pl.mbrzozowski.ranger.members.clan.hours.HoursService;
import pl.mbrzozowski.ranger.members.clan.rank.RankService;
import pl.mbrzozowski.ranger.model.BotWriter;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.server.service.ServerService;
import pl.mbrzozowski.ranger.server.service.transcription.TranscriptionService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WriteListener extends ListenerAdapter {

    private final EventsGeneratorService eventsGeneratorService;
    private final EventsSettingsService eventsSettingsService;
    private final UsersReminderService usersReminderService;
    private final TranscriptionService transcriptionService;
    private final GuildMembersService guildMembersService;
    private final RecruitsService recruitsService;
    private final DisboardService disboardService;
    private final ServerService serverService;
    private final EventService eventService;
    private final HoursService hoursService;
    private final RankService rankService;
    private final BotWriter botWriter;
    private final ContentService contentService;


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        log.info(event.getAuthor() + " - send message on channel {}", event.getChannel());

        ServerTicket serverTicket = new ServerTicket(serverService, transcriptionService);
        DisboardBot disboardBot = new DisboardBot(disboardService);
        ReminderCmd reminderCmd = new ReminderCmd(usersReminderService);
        LogChannel logChannel = new LogChannel();
        ClanMemberCheck clanMemberCheck = new ClanMemberCheck();
        GeneratorCmd generatorCmd = new GeneratorCmd(eventService, eventsGeneratorService);
        ServerServiceCmd serverServiceCmd = new ServerServiceCmd(serverService, contentService);
        HelpCmd helpCmd = new HelpCmd();
        AdminCheck adminCheck = new AdminCheck();
        EmbedSender embedSender = new EmbedSender();
        RecruitCmd recruitCmd = new RecruitCmd(recruitsService);
        CheckIsPrivateChannel checkIsPrivateChannel = new CheckIsPrivateChannel();
        AdminCommands adminCommands = new AdminCommands(guildMembersService, hoursService, rankService);
        EventsSettingsCmd eventsSettingsCmd = new EventsSettingsCmd(eventsSettingsService);
        DeveloperCmd developerCmd = new DeveloperCmd(eventService, botWriter);
        InvalidCmd invalidCmd = new InvalidCmd();

        serverTicket.setNextProccess(disboardBot);
        disboardBot.setNextProccess(reminderCmd);
        reminderCmd.setNextProccess(logChannel);
        logChannel.setNextProccess(clanMemberCheck);
        clanMemberCheck.setNextProccess(generatorCmd);
        generatorCmd.setNextProccess(serverServiceCmd);
        serverServiceCmd.setNextProccess(helpCmd);
        helpCmd.setNextProccess(adminCheck);
        adminCheck.setNextProccess(embedSender);
        embedSender.setNextProccess(recruitCmd);
        recruitCmd.setNextProccess(checkIsPrivateChannel);
        checkIsPrivateChannel.setNextProccess(adminCommands);
        adminCommands.setNextProccess(eventsSettingsCmd);
        eventsSettingsCmd.setNextProccess(developerCmd);
        developerCmd.setNextProccess(invalidCmd);

        serverTicket.proccessMessage(event);
    }
}




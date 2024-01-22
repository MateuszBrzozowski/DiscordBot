package pl.mbrzozowski.ranger.bot.events;


import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.bot.events.writing.*;
import pl.mbrzozowski.ranger.disboard.DisboardService;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.members.GuildMembersService;
import pl.mbrzozowski.ranger.model.BotWriter;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.role.RoleService;
import pl.mbrzozowski.ranger.server.service.ServerService;

@Slf4j
@Service
public class WriteListener extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;
    private final BotWriter botWriter;
    private final ServerService serverService;
    private final UsersReminderService usersReminderService;
    private final EventsGeneratorService eventsGeneratorService;
    private final EventsSettingsService eventsSettingsService;
    private final RoleService roleService;
    private final DisboardService disboardService;
    private final GuildMembersService guildMembersService;

    @Autowired
    public WriteListener(EventService eventService,
                         RecruitsService recruitsService,
                         BotWriter botWriter,
                         ServerService serverService,
                         UsersReminderService usersReminderService,
                         EventsGeneratorService eventsGeneratorService,
                         EventsSettingsService eventsSettingsService,
                         RoleService roleService,
                         DisboardService disboardService,
                         GuildMembersService guildMembersService) {
        this.eventService = eventService;
        this.recruitsService = recruitsService;
        this.botWriter = botWriter;
        this.serverService = serverService;
        this.usersReminderService = usersReminderService;
        this.eventsGeneratorService = eventsGeneratorService;
        this.eventsSettingsService = eventsSettingsService;
        this.roleService = roleService;
        this.disboardService = disboardService;
        this.guildMembersService = guildMembersService;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        log.info(event.getAuthor() + " - send message");

        DisboardBot disboardBot = new DisboardBot(disboardService);
        ReminderCmd reminderCmd = new ReminderCmd(usersReminderService);
        LogChannel logChannel = new LogChannel();
        ClanMemberCheck clanMemberCheck = new ClanMemberCheck();
        GeneratorCmd generatorCmd = new GeneratorCmd(eventService, eventsGeneratorService);
        ServerServiceCmd serverServiceCmd = new ServerServiceCmd(serverService);
        HelpCmd helpCmd = new HelpCmd();
        AdminCheck adminCheck = new AdminCheck();
        EmbedSender embedSender = new EmbedSender(roleService);
        RecruitCmd recruitCmd = new RecruitCmd(recruitsService);
        CheckIsPrivateChannel checkIsPrivateChannel = new CheckIsPrivateChannel();
        AdminCommands adminCommands = new AdminCommands(guildMembersService);
        EventsSettingsCmd eventsSettingsCmd = new EventsSettingsCmd(eventService, eventsSettingsService);
        DeveloperCmd developerCmd = new DeveloperCmd(eventService, botWriter);
        InvalidCmd invalidCmd = new InvalidCmd();

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

        disboardBot.proccessMessage(event);
    }
}




package pl.mbrzozowski.ranger.bot.events;


import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.bot.events.writing.*;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.model.BotWriter;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.role.RoleService;
import pl.mbrzozowski.ranger.server.service.ServerService;
import pl.mbrzozowski.ranger.stats.ServerStats;

@Slf4j
@Service
public class WriteListener extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;
    private final BotWriter botWriter;
    private final ServerService serverService;
    private final ServerStats serverStats;
    private final UsersReminderService usersReminderService;
    private final EventsGeneratorService eventsGeneratorService;
    private final EventsSettingsService eventsSettingsService;
    private final RoleService roleService;

    @Autowired
    public WriteListener(EventService eventService,
                         RecruitsService recruitsService,
                         BotWriter botWriter,
                         ServerService serverService,
                         ServerStats serverStats,
                         UsersReminderService usersReminderService,
                         EventsGeneratorService eventsGeneratorService,
                         EventsSettingsService eventsSettingsService,
                         RoleService roleService) {
        this.eventService = eventService;
        this.recruitsService = recruitsService;
        this.botWriter = botWriter;
        this.serverService = serverService;
        this.serverStats = serverStats;
        this.usersReminderService = usersReminderService;
        this.eventsGeneratorService = eventsGeneratorService;
        this.eventsSettingsService = eventsSettingsService;
        this.roleService = roleService;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        String contentDisplay = event.getMessage().getContentDisplay();

        Message msg = new Message(
                message,
                contentDisplay,
                event.getAuthor().getId(),
                event);
        log.info("[EVENT] - " + event.getAuthor().getName() + " - send message");

        CheckUser checkUser = new CheckUser(event);
        LogChannel logChannel = new LogChannel(event);
        GeneratorCmd generatorCmd = new GeneratorCmd(event, eventService, eventsGeneratorService);
        ReminderCmd reminderCmd = new ReminderCmd(event, usersReminderService);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin(event);
        EmbedSender embedSender = new EmbedSender(event, roleService);
        EventsSettingsCmd eventsSettingsCmd = new EventsSettingsCmd(event, eventService, eventsSettingsService);
        HelpCmd helpCmd = new HelpCmd(event);
        RecruitCmd recruitCmd = new RecruitCmd(event, recruitsService);
        DeveloperCmd developerCmd = new DeveloperCmd(event, eventService, botWriter);
        ServerServiceCmd serverServiceCmd = new ServerServiceCmd(event, serverService);
        InvalidCmd invalidCmd = new InvalidCmd(event);
        CheckIsPrivateChannel checkIsPrivateChannel = new CheckIsPrivateChannel(event);

        reminderCmd.setNextProccess(logChannel);
        logChannel.setNextProccess(checkUser);
        checkUser.setNextProccess(generatorCmd);
        generatorCmd.setNextProccess(serverServiceCmd);
        serverServiceCmd.setNextProccess(helpCmd);
        helpCmd.setNextProccess(checkUserAdmin);
        checkUserAdmin.setNextProccess(embedSender);
        embedSender.setNextProccess(recruitCmd);
        recruitCmd.setNextProccess(checkIsPrivateChannel);
        checkIsPrivateChannel.setNextProccess(eventsSettingsCmd);
        eventsSettingsCmd.setNextProccess(developerCmd);
        developerCmd.setNextProccess(invalidCmd);


        reminderCmd.proccessMessage(msg);
    }
}




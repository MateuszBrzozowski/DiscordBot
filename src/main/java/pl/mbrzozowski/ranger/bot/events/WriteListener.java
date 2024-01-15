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

    @Autowired
    public WriteListener(EventService eventService,
                         RecruitsService recruitsService,
                         BotWriter botWriter,
                         ServerService serverService,
                         UsersReminderService usersReminderService,
                         EventsGeneratorService eventsGeneratorService,
                         EventsSettingsService eventsSettingsService,
                         RoleService roleService,
                         DisboardService disboardService) {
        this.eventService = eventService;
        this.recruitsService = recruitsService;
        this.botWriter = botWriter;
        this.serverService = serverService;
        this.usersReminderService = usersReminderService;
        this.eventsGeneratorService = eventsGeneratorService;
        this.eventsSettingsService = eventsSettingsService;
        this.roleService = roleService;
        this.disboardService = disboardService;
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
        log.info(event.getAuthor() + " - send message");

        DisboardBot disboardBot = new DisboardBot(event, disboardService);
        ReminderCmd reminderCmd = new ReminderCmd(event, usersReminderService);
        LogChannel logChannel = new LogChannel(event);
        CheckUser checkUser = new CheckUser(event);
        GeneratorCmd generatorCmd = new GeneratorCmd(event, eventService, eventsGeneratorService);
        ServerServiceCmd serverServiceCmd = new ServerServiceCmd(event, serverService);
        HelpCmd helpCmd = new HelpCmd(event);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin(event);
        EmbedSender embedSender = new EmbedSender(event, roleService);
        RecruitCmd recruitCmd = new RecruitCmd(event, recruitsService);
        CheckIsPrivateChannel checkIsPrivateChannel = new CheckIsPrivateChannel(event);
        EventsSettingsCmd eventsSettingsCmd = new EventsSettingsCmd(event, eventService, eventsSettingsService);
        DeveloperCmd developerCmd = new DeveloperCmd(event, eventService, botWriter);
        InvalidCmd invalidCmd = new InvalidCmd(event);

        disboardBot.setNextProccess(reminderCmd);
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

        disboardBot.proccessMessage(msg);
    }
}




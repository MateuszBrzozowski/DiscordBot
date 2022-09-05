package ranger.bot.events;


import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ranger.bot.events.writing.*;
import ranger.event.EventService;
import ranger.recruit.RecruitsService;

@Service
public class WriteListener extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;

    @Autowired
    public WriteListener(EventService eventService, RecruitsService recruitsService) {
        this.eventService = eventService;
        this.recruitsService = recruitsService;
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

        DiceCmd diceCmd = new DiceCmd(event);
        StatsCmd statsCmd = new StatsCmd(event);
        CheckUser checkUser = new CheckUser(event);
        CounterMachine counterMachine = new CounterMachine(event);
        LogChannel logChannel = new LogChannel(event);
        GeneratorCmd generatorCmd = new GeneratorCmd(event, eventService);
        ReminderCmd reminderCmd = new ReminderCmd(event);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin(event);
        EmbedSender embedSender = new EmbedSender(event);
        EventsSettingsCmd eventsSettingsCmd = new EventsSettingsCmd(event, eventService);
        HelpCmd helpCmd = new HelpCmd(event);
        RecruitCmd recrutCmd = new RecruitCmd(event, recruitsService);
        DeveloperCmd developerCmd = new DeveloperCmd(event, eventService);
        Roles roles = new Roles(event);
        ServerServiceCmd serverServiceCmd = new ServerServiceCmd(event);
        InvalidCmd invalidCmd = new InvalidCmd(event);
        CheckIsPrivateChannel checkIsPrivateChannel = new CheckIsPrivateChannel(event);

        diceCmd.setNextProccess(statsCmd);
        statsCmd.setNextProccess(roles);
        roles.setNextProccess(logChannel);
        logChannel.setNextProccess(checkUser);
        checkUser.setNextProccess(counterMachine);
        counterMachine.setNextProccess(generatorCmd);
        generatorCmd.setNextProccess(reminderCmd);
        reminderCmd.setNextProccess(serverServiceCmd);
        serverServiceCmd.setNextProccess(helpCmd);
        helpCmd.setNextProccess(checkUserAdmin);
        checkUserAdmin.setNextProccess(embedSender);
        embedSender.setNextProccess(recrutCmd);
        recrutCmd.setNextProccess(checkIsPrivateChannel);
        checkIsPrivateChannel.setNextProccess(eventsSettingsCmd);
        eventsSettingsCmd.setNextProccess(developerCmd);
        developerCmd.setNextProccess(invalidCmd);


        diceCmd.proccessMessage(msg);
    }
}




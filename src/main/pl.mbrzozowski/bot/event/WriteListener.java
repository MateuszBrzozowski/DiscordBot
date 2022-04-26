package bot.event;


import bot.event.writing.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        String contentDisplay = event.getMessage().getContentDisplay();

        Message msg = new Message(message, contentDisplay, event.getAuthor().getId());

        DiceCmd diceCmd = new DiceCmd(event);
        StatsCmd statsCmd = new StatsCmd(event);
        CheckUser checkUser = new CheckUser(event);
        CounterMachine counterMachine = new CounterMachine(event);
        LogChannel logChannel = new LogChannel(event);
        GeneratorCmd generatorCmd = new GeneratorCmd(event);
        ReminderCmd reminderCmd = new ReminderCmd(event);
        QuestionnaireCmd questionnaire = new QuestionnaireCmd(event);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin(null);
        EmbedSender embedSender = new EmbedSender(event);
        EventsSettingsCmd eventsSettingsCmd = new EventsSettingsCmd(event);
        HelpCmd helpCmd = new HelpCmd(event);
        RecrutCmd recrutCmd = new RecrutCmd(event);
        DeveloperCmd developerCmd = new DeveloperCmd(event);
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
        generatorCmd.setNextProccess(questionnaire);
        questionnaire.setNextProccess(reminderCmd);
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




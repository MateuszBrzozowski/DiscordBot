package bot.event;


import bot.event.writing.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        String contentDisplay = event.getMessage().getContentDisplay();

        Message msg = new Message(message, contentDisplay, event.getAuthor().getId());


        DiceCmd diceCmd = new DiceCmd(event);
        CheckUser checkUser = new CheckUser();
        LogChannel logChannel = new LogChannel(event);
        GeneratorCmd generatorCmd = new GeneratorCmd(event);
        EventsCmd eventsCmd = new EventsCmd(event);
        ChannelCmd channelCmd = new ChannelCmd(event);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin(null);
        HelpCmd helpCmd = new HelpCmd(event);
        RecrutCmd recrutCmd = new RecrutCmd(event);

        diceCmd.setNextProccess(logChannel);
        logChannel.setNextProccess(checkUser);
        checkUser.setNextProccess(generatorCmd);
        generatorCmd.setNextProccess(eventsCmd);
        eventsCmd.setNextProccess(channelCmd);
        channelCmd.setNextProccess(helpCmd);
        helpCmd.setNextProccess(checkUserAdmin);
        checkUserAdmin.setNextProccess(recrutCmd);

        diceCmd.proccessMessage(msg);
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String[] message = event.getMessage().getContentRaw().split(" ");
        String contentDisplay = event.getMessage().getContentDisplay();

        Message msg = new Message(message, contentDisplay, event.getAuthor().getId());

        CheckUser checkUser = new CheckUser();
        GeneratorCmd generatorCmd = new GeneratorCmd(event);
        EventsCmd eventsCmd = new EventsCmd(event);
        ChannelCmd channelCmd = new ChannelCmd(event);
        HelpCmd helpCmd = new HelpCmd(event);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin(event);
        DeveloperCmd developerCmd = new DeveloperCmd(event);

        checkUser.setNextProccess(generatorCmd);
        generatorCmd.setNextProccess(eventsCmd);
        eventsCmd.setNextProccess(channelCmd);
        channelCmd.setNextProccess(helpCmd);
        helpCmd.setNextProccess(checkUserAdmin);
        checkUserAdmin.setNextProccess(developerCmd);

        checkUser.proccessMessage(msg);
    }
}




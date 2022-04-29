package bot.event.writing;

import embed.EmbedHelp;
import helpers.Commands;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCmd extends Proccess {

    public HelpCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getWords().length >= 1 && message.getWords()[0].equalsIgnoreCase(Commands.HELPS)) {
                EmbedHelp.help(message.getUserID(), message.getWords());
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

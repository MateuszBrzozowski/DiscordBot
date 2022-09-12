package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;

public class LogChannel extends Proccess {

    public LogChannel(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (messageReceived.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_RANGER_BOT_LOGGER)) {
            EmbedInfo.noWriteOnLoggerChannel(messageReceived);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

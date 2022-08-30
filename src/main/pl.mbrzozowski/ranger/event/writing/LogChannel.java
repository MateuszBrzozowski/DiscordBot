package ranger.event.writing;

import ranger.embed.EmbedInfo;
import ranger.helpers.CategoryAndChannelID;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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

package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.response.EmbedInfo;

@Slf4j
public class LogChannel extends Proccess {

    public LogChannel(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (messageReceived.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_RANGER_BOT_LOGGER)) {
            log.info(messageReceived.getAuthor() + " - wrote on Ranger log channel");
            EmbedInfo.noWriteOnLoggerChannel(messageReceived);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

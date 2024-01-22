package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.response.EmbedInfo;

@Slf4j
public class LogChannel extends Proccess {

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_RANGER_BOT_LOGGER)) {
            log.info(event.getAuthor() + " - wrote on Ranger log channel");
            EmbedInfo.noWriteOnLoggerChannel(event);
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

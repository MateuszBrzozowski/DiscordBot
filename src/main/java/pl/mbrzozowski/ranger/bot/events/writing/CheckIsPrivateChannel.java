package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class CheckIsPrivateChannel extends Proccess {

    public CheckIsPrivateChannel() {
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            getNextProccess().proccessMessage(event);
        }
    }
}

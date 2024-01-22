package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedHelp;

public class HelpCmd extends Proccess {

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.HELPS)) {
                EmbedHelp.help(event.getAuthor(), event.getMessage().getContentRaw());
            } else {
                getNextProccess().proccessMessage(event);
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

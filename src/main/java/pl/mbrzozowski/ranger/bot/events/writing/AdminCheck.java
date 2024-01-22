package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Users;

public class AdminCheck extends Proccess {

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (Users.isAdmin(event.getAuthor().getId())) {
            getNextProccess().proccessMessage(event);
        } else {
            if (event.isFromType(ChannelType.PRIVATE) && !event.getAuthor().isBot()) {
                InvalidCmd invalidCmd = new InvalidCmd();
                invalidCmd.proccessMessage(event);
            }
        }
    }
}

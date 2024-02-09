package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.guild.Commands;
import pl.mbrzozowski.ranger.response.EmbedHelp;
import pl.mbrzozowski.ranger.response.EmbedInfo;

public class ReminderCmd extends Proccess {

    private final UsersReminderService usersReminderService;

    public ReminderCmd(UsersReminderService usersReminderService) {
        this.usersReminderService = usersReminderService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.HELP_REMINDER)) {
                EmbedHelp.help(event.getAuthor(), event.getMessage().getContentRaw());
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.REMINDER_OFF)) {
                usersReminderService.add(event.getAuthor());
                EmbedInfo.reminderOff(event.getAuthor().getId());
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.REMINDER_ON)) {
                usersReminderService.deleteByUserId(event.getAuthor());
                EmbedInfo.reminderOn(event.getAuthor().getId());
            } else {
                getNextProccess().proccessMessage(event);
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}

package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedHelp;
import pl.mbrzozowski.ranger.response.EmbedInfo;

public class ReminderCmd extends Proccess {

    private final UsersReminderService usersReminderService;

    public ReminderCmd(MessageReceivedEvent messageReceived, UsersReminderService usersReminderService) {
        super(messageReceived);
        this.usersReminderService = usersReminderService;
    }

    @Override
    public void proccessMessage(Message message) {
        if (messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.HELPS) && message.getWords()[1].equalsIgnoreCase(EmbedHelp.REMINDER)) {
                EmbedHelp.help(message.getUserID(), message.getWords());
            } else if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_OFF)) {
                usersReminderService.add(messageReceived.getAuthor().getId());
                EmbedInfo.reminderOff(message.getUserID());
            } else if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_ON)) {
                usersReminderService.deleteByUserId(messageReceived.getAuthor().getId());
                EmbedInfo.reminderOn(message.getUserID());
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

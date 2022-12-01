package pl.mbrzozowski.ranger.bot.events.writing;

import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.event.reminder.UsersReminderService;
import pl.mbrzozowski.ranger.helpers.RangerLogger;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReminderCmd extends Proccess {

    private final UsersReminderService usersReminderService;

    public ReminderCmd(MessageReceivedEvent messageReceived, UsersReminderService usersReminderService) {
        super(messageReceived);
        this.usersReminderService = usersReminderService;
    }

    @Override
    public void proccessMessage(Message message) {
        if (messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_OFF)) {
                usersReminderService.add(messageReceived.getAuthor().getId());
                EmbedInfo.reminderOff(message.getUserID());
                RangerLogger.info("Użykownik: [" + Users.getUserNicknameFromID(message.getUserID()) + "] wyłączył powiadomienia dla evnetów.");
            } else if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_ON)) {
                usersReminderService.deleteByUserId(messageReceived.getAuthor().getId());
                EmbedInfo.reminderOn(message.getUserID());
                RangerLogger.info("Użykownik: [" + Users.getUserNicknameFromID(message.getUserID()) + "] włączył powiadomienia dla eventów.");
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

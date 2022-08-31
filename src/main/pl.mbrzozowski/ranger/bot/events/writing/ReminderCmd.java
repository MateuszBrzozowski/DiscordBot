package ranger.bot.events.writing;

import ranger.embed.EmbedInfo;
import ranger.event.EventService;
import ranger.event.reminder.UsersReminderOFF;
import ranger.helpers.Commands;
import ranger.helpers.RangerLogger;
import ranger.helpers.Users;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReminderCmd extends Proccess {

    public ReminderCmd(MessageReceivedEvent messageReceived, EventService eventService) {
        super(eventService, messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_OFF)) {
                UsersReminderOFF usersReminderOFF = new UsersReminderOFF();
                usersReminderOFF.add(messageReceived.getAuthor().getId());
                EmbedInfo.reminderOff(message.getUserID());
                RangerLogger.info("Użykownik: [" + Users.getUserNicknameFromID(message.getUserID()) + "] wyłączył powiadomienia dla evnetów.");
            } else if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_ON)) {
                UsersReminderOFF usersReminderOFF = new UsersReminderOFF();
                usersReminderOFF.remove(messageReceived.getAuthor().getId());
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

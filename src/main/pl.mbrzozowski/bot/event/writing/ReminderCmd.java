package bot.event.writing;

import embed.EmbedInfo;
import event.reminder.UsersReminderOFF;
import helpers.Commands;
import helpers.RangerLogger;
import helpers.Users;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class ReminderCmd extends Proccess {

    PrivateMessageReceivedEvent event;

    public ReminderCmd(@NotNull PrivateMessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_OFF)){
            UsersReminderOFF usersReminderOFF = new UsersReminderOFF();
            usersReminderOFF.add(event.getAuthor().getId());
            EmbedInfo.reminderOff(message.getUserID());
            RangerLogger.info("Użykownik: ["+ Users.getUserNicknameFromID(message.getUserID()) +"] wyłączył powiadomienia dla evnetów.");
        } else if (message.getContentDisplay().equalsIgnoreCase(Commands.REMINDER_ON)){
            UsersReminderOFF usersReminderOFF = new UsersReminderOFF();
            usersReminderOFF.remove(event.getAuthor().getId());
            EmbedInfo.reminderOn(message.getUserID());
            RangerLogger.info("Użykownik: ["+ Users.getUserNicknameFromID(message.getUserID()) +"] włączył powiadomienia dla eventów.");
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

package bot.event.writing;

import bot.event.RoleEditor;
import helpers.Commands;
import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Roles extends Proccess {

    public Roles(MessageReceivedEvent messageReceivedEvent) {
        super(messageReceivedEvent);
    }

    @Override
    public void proccessMessage(Message message) {
        RoleEditor roleEditor = new RoleEditor();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TARKOV)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.TARKOV);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.VIRTUAL_REALITY)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.VIRTUAL_REALITY);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    private void sendMessageNoClanMember(String userID) {
        messageReceived.getChannel().sendMessage("*" + Users.getUserNicknameFromID(userID) + "*, Rola niedostępna.").queue();
    }

}

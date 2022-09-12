package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.bot.events.RoleEditor;

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
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.SQUAD);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.CS)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.CS);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.WAR_THUNDER)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.WAR_THUNDER);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.MINECRAFT)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.MINECRAFT);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.RAINBOW_SIX)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.RAINBOW_SIX);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.WARGAME)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.WARGAME);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.ARMA)) {
            roleEditor.addRemoveRole(message.getUserID(), RoleID.ARMA);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    private void sendMessageNoClanMember(String userID) {
        messageReceived.getChannel().sendMessage("*" + Users.getUserNicknameFromID(userID) + "*, Rola niedostępna.").queue();
    }

}

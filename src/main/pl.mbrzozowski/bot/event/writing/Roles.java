package bot.event.writing;

import bot.event.RoleEditor;
import helpers.Commands;
import helpers.Users;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

public class Roles extends Proccess {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    private String userID;
    private JDA jda = Repository.getJda();

    public Roles(MessageReceivedEvent messageReceivedEvent) {
        super(messageReceivedEvent);
        this.userID = messageReceived.getAuthor().getId();
    }

    @Override
    public void proccessMessage(Message message) {
        RoleEditor roleEditor = new RoleEditor();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TARKOV)) {
            if (message.isClanMember()) {
                roleEditor.addRemoveRole(userID, Commands.TARKOV);
            } else {
                sendMessageNoClanMember();
            }
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.VIRTUAL_REALITY)) {
            roleEditor.addRemoveRole(userID, Commands.VIRTUAL_REALITY);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    private void sendMessageNoClanMember() {
        messageReceived.getChannel().sendMessage("*" + Users.getUserNicknameFromID(userID) + "*, Rola niedostępna.").queue();
    }

}

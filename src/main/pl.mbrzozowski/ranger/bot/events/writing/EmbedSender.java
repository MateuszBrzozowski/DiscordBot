package ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.embed.EmbedInfo;
import ranger.helpers.Commands;

public class EmbedSender extends Proccess {

    public EmbedSender(MessageReceivedEvent messageReceivedEvent) {
        super(messageReceivedEvent);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.seedersRoleJoining(messageReceived.getTextChannel());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.ROLES)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.sendRoles(messageReceived);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.RECRUT_OPINIONS)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.recrutOpinionsFormOpening(messageReceived);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

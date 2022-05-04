package bot.event.writing;

import embed.EmbedInfo;
import embed.EmbedServerRules;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbedSender extends Proccess {

    public EmbedSender(MessageReceivedEvent messageReceivedEvent) {
        super(messageReceivedEvent);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.seedersRoleJoining(messageReceived.getTextChannel());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SERVER_RULES)) {
            messageReceived.getMessage().delete().submit();
            EmbedServerRules.sendServerRules(messageReceived);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

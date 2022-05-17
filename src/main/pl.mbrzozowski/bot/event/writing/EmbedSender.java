package bot.event.writing;

import embed.EmbedInfo;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;
import stats.ServerStats;

public class EmbedSender extends Proccess {

    public EmbedSender(MessageReceivedEvent messageReceivedEvent) {
        super(messageReceivedEvent);
    }

    @Override
    public void proccessMessage(Message message) {
        ServerStats serverStats = Repository.getServerStats();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.seedersRoleJoining(messageReceived.getTextChannel());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATS_MAPS)) {
            messageReceived.getMessage().delete().submit();
            serverStats.sendMapsStats(messageReceived);
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

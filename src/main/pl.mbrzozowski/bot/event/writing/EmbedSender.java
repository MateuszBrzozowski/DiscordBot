package bot.event.writing;

import embed.EmbedInfo;
import embed.EmbedServerRules;
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
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SERVER_RULES)) {
            messageReceived.getMessage().delete().submit();
            EmbedServerRules.sendServerRules(messageReceived);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATS_MAPS)) {
            serverStats.sendMapsStats(messageReceived);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

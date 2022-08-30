package ranger.event.writing;

import ranger.embed.EmbedInfo;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;
import ranger.stats.MapsStats;
import ranger.stats.ServerStats;

public class StatsCmd extends Proccess {

    public StatsCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        ServerStats serverStats = Repository.getServerStats();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATS)) {
            if (messageReceived.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                if (serverStats.isUserConnected(messageReceived.getAuthor().getId())) {
                    serverStats.viewStatsForUser(messageReceived.getAuthor().getId(), messageReceived.getTextChannel());
                } else {
                    EmbedInfo.notConnectedAccount(messageReceived.getTextChannel());
                }
            } else {
                EmbedInfo.youCanCheckStatsOnChannel(messageReceived.getTextChannel());
            }
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.STATS_PROFILE)) {
            if (messageReceived.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                if (serverStats.connectUserToSteam(messageReceived.getAuthor().getId(), message.getWords()[1])) {
                    EmbedInfo.connectSuccessfully(messageReceived.getTextChannel());
                    serverStats.viewStatsForUser(messageReceived.getAuthor().getId(), messageReceived.getTextChannel());
                } else {
                    EmbedInfo.connectUnSuccessfully(messageReceived.getTextChannel());
                }
            } else {
                EmbedInfo.youCanLinkedYourProfileOnChannel(messageReceived.getTextChannel());
            }
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATS_LAST_TEN_MAPS)) {
            if (messageReceived.getTextChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_ADMIN_CZAT)) {
                if (message.isClanMember()) {
                    MapsStats.showLastTenMaps(messageReceived);
                    messageReceived.getMessage().delete().submit();
                }
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

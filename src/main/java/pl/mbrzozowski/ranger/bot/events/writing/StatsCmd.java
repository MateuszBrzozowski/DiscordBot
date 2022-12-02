package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.transaction.CannotCreateTransactionException;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.stats.ServerStats;

@Slf4j
public class StatsCmd extends Proccess {

    private final ServerStats serverStats;

    public StatsCmd(MessageReceivedEvent messageReceived, ServerStats serverStats) {
        super(messageReceived);
        this.serverStats = serverStats;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATS)) {
            if (messageReceived.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                try {
                    if (serverStats.isUserConnected(messageReceived.getAuthor().getId())) {
                        serverStats.viewStatsForUser(messageReceived.getAuthor().getId(), messageReceived.getChannel().asTextChannel());
                    } else {
                        EmbedInfo.notConnectedAccount(message.getUserID(), messageReceived.getChannel().asTextChannel());
                    }
                } catch (CannotCreateTransactionException exception) {
                    log.error("Cannot create transaction exception. " + exception.getMessage());
                    EmbedInfo.cannotConnectStatsDB(message.getUserID(), messageReceived.getChannel().asTextChannel());
                }
            } else {
                EmbedInfo.youCanCheckStatsOnChannel(messageReceived.getChannel().asTextChannel());
            }
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.STATS_PROFILE)) {
            if (messageReceived.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                try {
                    if (serverStats.connectUserToSteam(messageReceived.getAuthor().getId(), message.getWords()[1])) {
                        EmbedInfo.connectSuccessfully(message.getUserID(), messageReceived.getChannel().asTextChannel());
                        serverStats.viewStatsForUser(messageReceived.getAuthor().getId(), messageReceived.getChannel().asTextChannel());
                    } else {
                        EmbedInfo.connectUnSuccessfully(message.getUserID(), messageReceived.getChannel().asTextChannel());
                    }
                } catch (CannotCreateTransactionException exception) {
                    log.error("Cannot create transaction exception. " + exception.getMessage());
                    EmbedInfo.cannotConnectStatsDB(message.getUserID(), messageReceived.getChannel().asTextChannel());
                }
            } else {
                EmbedInfo.youCanLinkedYourProfileOnChannel(messageReceived.getChannel().asTextChannel());
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

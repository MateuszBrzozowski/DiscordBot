package bot.event.writing;

import embed.EmbedInfo;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;
import stats.ServerStats;

public class StatsCmd extends Proccess {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    public StatsCmd(GuildMessageReceivedEvent event) {
        super.setGuildEvent(event);
    }

    @Override
    public void proccessMessage(Message message) {
        ServerStats serverStats = Repository.getServerStats();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATS)) {
            if (guildEvent.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                if (serverStats.isUserConnected(guildEvent.getAuthor().getId())) {
                    serverStats.viewStatsForUser(guildEvent.getAuthor().getId(), guildEvent.getChannel());
                } else {
                    EmbedInfo.notConnectedAccount(guildEvent.getChannel());
                }
            } else {
                EmbedInfo.youCanCheckStatsOnChannel(guildEvent.getChannel());
            }
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.STATS_PROFILE)) {
            if (guildEvent.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                if (serverStats.connectUserToSteam(guildEvent.getAuthor().getId(), message.getWords()[1])) {
                    EmbedInfo.connectSuccessfully(guildEvent.getChannel());
                } else {
                    EmbedInfo.connectUnSuccessfully(guildEvent.getChannel());
                }
            } else {
                EmbedInfo.youCanCheckStatsOnChannel(guildEvent.getChannel());
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

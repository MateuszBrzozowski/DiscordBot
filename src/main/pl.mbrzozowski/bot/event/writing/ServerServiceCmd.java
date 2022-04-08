package bot.event.writing;

import embed.EmbedInfo;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import ranger.Repository;
import server.service.ServerService;

public class ServerServiceCmd extends Proccess {

    public ServerServiceCmd(GuildMessageReceivedEvent event) {
        super.setGuildEvent(event);
    }

    @Override
    public void proccessMessage(Message message) {
        String channelID = guildEvent.getChannel().getId();
        ServerService serverService = Repository.getServerService();
        boolean isChannelSS = serverService.isChannelOnList(channelID);
        if (message.getWords()[0].equalsIgnoreCase(Commands.EMBED_SERVER_SERVICE)) {
            guildEvent.getMessage().delete().submit();
            EmbedInfo.serverService(guildEvent.getChannel());
        } else if (isChannelSS && message.getWords()[0].equalsIgnoreCase(Commands.CLOSE)) {
            guildEvent.getMessage().delete().submit();
            serverService.closeChannel(guildEvent);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

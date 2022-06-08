package bot.event.writing;

import embed.EmbedInfo;
import helpers.Commands;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;
import server.service.ServerService;
import server.whitelist.Whitelist;

public class ServerServiceCmd extends Proccess {

    public ServerServiceCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        String channelID = messageReceived.getChannel().getId();
        ServerService serverService = Repository.getServerService();
        boolean isChannelSS = serverService.isChannelOnList(channelID);
        if (message.getWords()[0].equalsIgnoreCase(Commands.EMBED_SERVER_SERVICE) && message.isAdmin()) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.serverService(messageReceived.getChannel());
        } else if (isChannelSS && message.getWords()[0].equalsIgnoreCase(Commands.CLOSE)) {
            messageReceived.getMessage().delete().submit();
            serverService.closeChannel(messageReceived);
        } else if (message.getWords()[0].equalsIgnoreCase(Commands.UPDATE_WL) && message.isAdmin() && messageReceived.isFromType(ChannelType.PRIVATE)) {
            new Thread(() -> {
                Whitelist whitelist = new Whitelist();
                whitelist.whitelistUpdate();
            }).start();
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

package ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.embed.EmbedInfo;
import ranger.helpers.Commands;
import ranger.server.service.ServerService;
import ranger.server.whitelist.Whitelist;

public class ServerServiceCmd extends Proccess {

    private final ServerService serverService;

    public ServerServiceCmd(MessageReceivedEvent messageReceived, ServerService serverService) {
        super(messageReceived);
        this.serverService = serverService;
    }

    @Override
    public void proccessMessage(Message message) {
        String channelID = messageReceived.getChannel().getId();
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

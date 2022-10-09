package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.server.service.ServerService;

public class ServerServiceCmd extends Proccess {

    private final ServerService serverService;

    public ServerServiceCmd(MessageReceivedEvent messageReceived, ServerService serverService) {
        super(messageReceived);
        this.serverService = serverService;
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        String channelID = messageReceived.getChannel().getId();
        if (message.getWords()[0].equalsIgnoreCase(Commands.EMBED_SERVER_SERVICE) && message.isAdmin()) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.serverService(messageReceived.getChannel());
        } else if (message.getWords()[0].equalsIgnoreCase(Commands.CLOSE) && isServerCategory(channelID)) {
            serverService.closeChannel(messageReceived);
        } else if (message.getWords()[0].equalsIgnoreCase(Commands.UPDATE_WL) && message.isAdmin() && messageReceived.isFromType(ChannelType.PRIVATE)) {
            messageReceived.getMessage().reply("Usługa wyłączona").queue();
//            new Thread(() -> {
//                Whitelist whitelist = new Whitelist();
//                whitelist.whitelistUpdate();
//            }).start();
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    private boolean isServerCategory(String channelID) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelID);
        if (textChannel != null) {
            Category parentCategory = textChannel.getParentCategory();
            if (parentCategory != null) {
                return parentCategory.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_SERVER);
            }
        }
        return false;
    }
}

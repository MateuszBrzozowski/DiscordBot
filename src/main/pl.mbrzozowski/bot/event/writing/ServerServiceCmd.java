package bot.event.writing;

import embed.EmbedInfo;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ServerServiceCmd extends Proccess {

    public ServerServiceCmd(GuildMessageReceivedEvent event) {
        super.setGuildEvent(event);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords()[0].equalsIgnoreCase(Commands.EMBED_SERVER_SERVICE)) {
            EmbedInfo.serverService(guildEvent.getChannel());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

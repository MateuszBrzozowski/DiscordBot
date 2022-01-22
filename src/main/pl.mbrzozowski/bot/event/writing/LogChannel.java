package bot.event.writing;

import embed.EmbedInfo;
import helpers.CategoryAndChannelID;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LogChannel extends Proccess{

    private GuildMessageReceivedEvent guildEvent;

    public LogChannel(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (guildEvent.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_RANGER_BOT_LOGGER)){
            EmbedInfo.noWriteOnLoggerChannel(guildEvent);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

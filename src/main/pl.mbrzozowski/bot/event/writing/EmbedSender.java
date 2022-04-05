package bot.event.writing;

import embed.EmbedInfo;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EmbedSender extends Proccess {

    private GuildMessageReceivedEvent guildEvent;

    public EmbedSender(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            EmbedInfo.seedersRoleJoining(guildEvent.getChannel());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

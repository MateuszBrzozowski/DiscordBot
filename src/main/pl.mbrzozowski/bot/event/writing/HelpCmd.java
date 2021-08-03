package bot.event.writing;

import embed.EmbedHelp;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class HelpCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;
    private PrivateMessageReceivedEvent privateEvent;

    public HelpCmd(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    public HelpCmd(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }


    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length >= 1 && message.getWords()[0].equalsIgnoreCase(Commands.HELPS)) {
            if (guildEvent != null) {
                guildEvent.getMessage().delete().submit();
            }
            EmbedHelp.help(message.getUserID(), message.getWords());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

package bot.event.writing;

import embed.EmbedInfo;
import embed.Recruiter;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RecrutCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;

    public RecrutCmd(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }


    @Override
    public void proccessMessage(Message message) {
        String cmd = message.getWords()[0];
        if (cmd.equalsIgnoreCase(Commands.START_REKRUT)) {
            guildEvent.getMessage().delete().submit();
            new Recruiter(guildEvent);
        } else if (cmd.equalsIgnoreCase(Commands.NEGATIVE)) {
            guildEvent.getMessage().delete().submit();
            EmbedInfo.endNegative(guildEvent.getAuthor().getId(), guildEvent.getChannel());
        } else if (cmd.equalsIgnoreCase(Commands.POSITIVE)) {
            guildEvent.getMessage().delete().submit();
            EmbedInfo.endPositive(guildEvent.getAuthor().getId(), guildEvent.getChannel());
        } else if (cmd.equalsIgnoreCase(Commands.REOPEN)) {
            guildEvent.getMessage().delete().submit();
            getRecruits().reOpenChannel(guildEvent);
        } else if (cmd.equalsIgnoreCase(Commands.CLOSE)) {
            guildEvent.getMessage().delete().submit();
            getRecruits().closeChannel(guildEvent);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.ACCEPT_RECRUT)) {
//            guildEvent.getMessage().delete().submit();
            getRecruits().acceptRecrut(message.getWords()[1], guildEvent.getChannel(), guildEvent.getAuthor());
        }
    }


}

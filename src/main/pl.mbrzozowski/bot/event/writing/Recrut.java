package bot.event.writing;

import embed.EmbedInfo;
import embed.Recruiter;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class Recrut extends Category {

    private GuildMessageReceivedEvent guildEvent;

    public Recrut(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }


    @Override
    void proccessMessage(Message message) {
        if (message.isAdmin()) {
            String cmd = message.getWords()[0];
            switch (cmd) {
                case Commands.START_REKRUT:
                    new Recruiter(guildEvent);
                    break;
                case Commands.POSITIVE:
                    EmbedInfo.endPositive(guildEvent.getAuthor().getId(), guildEvent.getChannel());
                    break;
                case Commands.NEGATIVE:
                    EmbedInfo.endNegative(guildEvent.getAuthor().getId(), guildEvent.getChannel());
                    break;
                case Commands.CLOSE:
                    getRecruits().closeChannel(guildEvent);
                    break;
                case Commands.REOPEN:
                    getRecruits().reOpenChannel(guildEvent);
                    break;
                case Commands.REMOVE_CHANNEL:
                    if (getRecruits().isRecruitChannel(guildEvent.getChannel().getId())) {
                        getRecruits().deleteChannel(guildEvent);
                    } else {
                        getNextCategory().proccessMessage(message);
                    }
                    break;
                default:
                    getNextCategory().proccessMessage(message);
            }


//            if (cmd.equalsIgnoreCase(Commands.START_REKRUT)) {
//                new Recruiter(guildEvent);
//            } else if (cmd.equalsIgnoreCase(Commands.NEGATIVE)) {
//                EmbedInfo.endNegative(guildEvent.getAuthor().getId(), guildEvent.getChannel());
//            } else if (cmd.equalsIgnoreCase(Commands.POSITIVE)) {
//                EmbedInfo.endPositive(guildEvent.getAuthor().getId(), guildEvent.getChannel());
//            } else if (cmd.equalsIgnoreCase(Commands.REOPEN)) {
//                getRecruits().reOpenChannel(guildEvent);
//            } else if (cmd.equalsIgnoreCase(Commands.CLOSE)) {
//                getRecruits().closeChannel(guildEvent);
//            } else if (cmd.equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
//                String channelD = guildEvent.getChannel().getId();
//                if (getRecruits().isRecruitChannel(channelD)) {
//                    getRecruits().deleteChannel(guildEvent);
//                } else {
//                    getNextCategory().proccessMessage(message);
//                }
//            }
        } else {
            getNextCategory().proccessMessage(message);
        }
    }
}

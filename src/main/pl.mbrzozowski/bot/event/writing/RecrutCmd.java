package bot.event.writing;

import embed.EmbedInfo;
import embed.Recruiter;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RecrutCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;

//    public Recrut() {
//        this.guildEvent = guildEvent;
//    }


    @Override
    public void proccessMessage(Message message) {
        if (message.isAdmin()) {
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
            } else if (cmd.equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
                String channelD = guildEvent.getChannel().getId();
                if (getRecruits().isRecruitChannel(channelD)) {
                    guildEvent.getMessage().delete().submit();
                    getRecruits().deleteChannel(guildEvent);
                } else {
                    getNextProccess().proccessMessage(message);
                }
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }


}

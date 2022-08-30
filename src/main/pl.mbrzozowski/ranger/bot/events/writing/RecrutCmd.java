package ranger.bot.events.writing;

import ranger.embed.EmbedInfo;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RecrutCmd extends Proccess {

    public RecrutCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        String cmd = message.getWords()[0];
        if (!message.isPrivate()) {
            String parentCategoryId = messageReceived.getTextChannel().getParentCategoryId();
            if (cmd.equalsIgnoreCase(Commands.START_REKRUT)) {
                messageReceived.getMessage().delete().submit();
                EmbedInfo.recruiter(messageReceived);
            } else if (cmd.equalsIgnoreCase(Commands.NEGATIVE)) {
                messageReceived.getMessage().delete().submit();
                if (!getRecruits().isResult(messageReceived.getTextChannel())) {
                    EmbedInfo.endNegative(messageReceived.getAuthor().getId(), messageReceived.getTextChannel());
                    getRecruits().negativeResult(messageReceived.getTextChannel());
                }
            } else if (cmd.equalsIgnoreCase(Commands.POSITIVE)) {
                messageReceived.getMessage().delete().submit();
                if (!getRecruits().isResult(messageReceived.getTextChannel())) {
                    EmbedInfo.endPositive(messageReceived.getAuthor().getId(), messageReceived.getTextChannel());
                    getRecruits().positiveResult(messageReceived.getTextChannel());
                }
            } else if (cmd.equalsIgnoreCase(Commands.REOPEN)) {
                messageReceived.getMessage().delete().submit();
                getRecruits().reOpenChannel(messageReceived);
            } else if (cmd.equalsIgnoreCase(Commands.CLOSE) && parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                messageReceived.getMessage().delete().submit();
                getRecruits().closeChannel(messageReceived);
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

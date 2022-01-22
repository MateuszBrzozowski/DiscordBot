package bot.event.writing;

import embed.EmbedInfo;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class DeveloperCmd extends Proccess {

    private PrivateMessageReceivedEvent privateEvent;

    public DeveloperCmd(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            getEvents().disableButtons(message.getWords()[1]);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            getEvents().disableButtons(message.getWords()[1], message.getWords()[2]);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            getEvents().enableButtons(message.getWords()[1]);
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            getEvents().enableButtons(message.getWords()[1], message.getWords()[2]);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.STATUS)) {
            EmbedInfo.sendStatus(privateEvent.getAuthor().getId());
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.MSG)) {
            getBotWriter().setChannelID(message.getWords()[1]);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.MSG_CANCEL)) {
            getBotWriter().setActive(false);
        } else if (getBotWriter().isActive()) {
            getBotWriter().sendMsg(privateEvent.getMessage().getContentDisplay());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

package ranger.bot.events.writing;

import ranger.embed.EmbedInfo;
import ranger.helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DeveloperCmd extends Proccess {

    public DeveloperCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
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
            EmbedInfo.sendStatus(messageReceived.getAuthor().getId());
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.MSG)) {
            getBotWriter().setChannelID(message.getWords()[1]);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.MSG_CANCEL)) {
            getBotWriter().setActive(false);
        } else if (getBotWriter().isActive()) {
            getBotWriter().sendMsg(messageReceived.getMessage().getContentDisplay());
        } else if (message.getWords().length == 3 && message.getWords()[0].equalsIgnoreCase(Commands.REMOVE_USER_FROM_EVENT)) {
            getEvents().removeUserFromEvent(message.getWords()[1], message.getWords()[2]);
        } else if (message.getWords().length == 2 && message.getWords()[0].equalsIgnoreCase(Commands.REMOVE_USER_FROM_EVENTS)) {
            getEvents().removeUserFromAllEvents(message.getWords()[1]);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

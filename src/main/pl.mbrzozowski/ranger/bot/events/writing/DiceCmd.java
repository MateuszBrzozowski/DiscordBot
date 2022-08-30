package ranger.bot.events.writing;

import ranger.helpers.Commands;
import ranger.model.DiceGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiceCmd extends Proccess {

    public DiceCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (!messageReceived.isFromType(ChannelType.PRIVATE)) {
            if (message.getWords()[0].equalsIgnoreCase(Commands.DICE)) {
                messageReceived.getMessage().delete().submit();
                if (message.getWords().length == 1) {
                    DiceGame diceGame = new DiceGame();
                    diceGame.playSolo(messageReceived);
                } else if (message.getWords().length > 1) {
                    DiceGame diceGame = new DiceGame(message.getWords(), messageReceived);
                    getDice().addGame(diceGame);
                }
            } else if (!messageReceived.getAuthor().isBot()) {
                if (getDice().isActiveGameOnChannelID(messageReceived.getChannel().getId())) {
                    messageReceived.getMessage().delete().submit();
                    getDice().play(messageReceived);
                } else {
                    getNextProccess().proccessMessage(message);
                }
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

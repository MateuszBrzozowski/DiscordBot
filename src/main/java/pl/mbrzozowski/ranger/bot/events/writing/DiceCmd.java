package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.dice.DiceGame;
import pl.mbrzozowski.ranger.dice.DiceGames;
import pl.mbrzozowski.ranger.helpers.Commands;

public class DiceCmd extends Proccess {

    private final DiceGames diceGames;

    public DiceCmd(MessageReceivedEvent messageReceived, DiceGames diceGames) {
        super(messageReceived);
        this.diceGames = diceGames;
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
                    diceGames.addGame(diceGame);
                }
            } else if (!messageReceived.getAuthor().isBot()) {
                if (diceGames.isActiveGameOnChannelID(messageReceived.getChannel().getId())) {
                    messageReceived.getMessage().delete().submit();
                    diceGames.play(messageReceived);
                } else {
                    getNextProccess().proccessMessage(message);
                }
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

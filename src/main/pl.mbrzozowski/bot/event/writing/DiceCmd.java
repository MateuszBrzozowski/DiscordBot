package bot.event.writing;

import helpers.Commands;
import helpers.RangerLogger;
import model.DiceGame;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DiceCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;

    public DiceCmd(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords()[0].equalsIgnoreCase(Commands.DICE)) {
            guildEvent.getMessage().delete().submit();
            if (message.getWords().length == 1) {
                DiceGame diceGame = new DiceGame();
                diceGame.playSolo(guildEvent);
            } else if (message.getWords().length > 1) {
                DiceGame diceGame = new DiceGame(message.getWords(), guildEvent);
                getDice().addGame(diceGame);
            }
        } else if (!guildEvent.getAuthor().isBot()) {
            if (getDice().isActiveGameOnChannelID(guildEvent.getChannel().getId())) {
                guildEvent.getMessage().delete().submit();
                getDice().play(guildEvent);
            } else {
                getNextProccess().proccessMessage(message);
            }
        }
    }
}

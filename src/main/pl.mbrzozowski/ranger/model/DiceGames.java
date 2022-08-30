package ranger.model;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class DiceGames {
    private List<DiceGame> diceGames = new ArrayList<>();

    public void addGame(DiceGame diceGame) {
        diceGames.add(diceGame);
    }

    public boolean isActiveGameOnChannelID(String id) {
        for (DiceGame dg : diceGames) {
            if (dg.getChannelID().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    public void play(MessageReceivedEvent event) {
        int index = getIndexOfGame(event.getChannel().getId());
        diceGames.get(index).play(event);
        diceGames.remove(index);
    }

    private int getIndexOfGame(String id) {
        for (int i = 0; i < diceGames.size(); i++) {
            if (diceGames.get(i).getChannelID().equalsIgnoreCase(id)) return i;
        }
        return -1;
    }
}

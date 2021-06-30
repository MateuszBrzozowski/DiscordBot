package events;

import model.ActiveMatch;
import model.SignUpMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import model.Recruits;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ButtonClickListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private static SignUpMatch matches;

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        matches = RangerBot.getMatches();
        if (event.getComponentId().equals("newRecrut")) {
            Recruits recrut = RangerBot.getRecruits();
            recrut.newPodanie(event);
        }

        if (matches.isActiveMatch(event.getChannel().getId())>=0){
            int indexOfMatch = matches.isActiveMatch(event.getChannel().getId());
            logger.info("Dane meczu prawidlowo odczytane.");
            event.deferEdit().queue();
            if (event.getComponentId().equalsIgnoreCase("in_"+event.getChannel().getId())){
                matches.signIn(event,indexOfMatch);
                matches.updateEmbed(event, indexOfMatch);
            } else if (event.getComponentId().equalsIgnoreCase("reserve_"+event.getChannel().getId())){
                matches.signINReserve(event,indexOfMatch);
                //TODO zzaktualizwoać EMbed
            }else if (event.getComponentId().equalsIgnoreCase("out_"+event.getChannel().getId())){
                matches.signOut(event,indexOfMatch);
                //TODO zzaktualizwoać EMbed
            }
        }

    }
}

package events;

import model.Recruits;
import model.Event;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class ButtonClickListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private static Event matches;

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getComponentId().equals("newRecrut")) {
            Recruits recrut = RangerBot.getRecruits();
            recrut.newPodanie(event);
        }

        matches = RangerBot.getMatches();
        if (matches.isActiveMatch(event.getChannel().getId())>=0){
            logger.info("Dane meczu prawidlowo odczytane.");
            int indexOfMatch = matches.isActiveMatch(event.getChannel().getId());
            event.deferEdit().queue();
            if (event.getComponentId().equalsIgnoreCase("in_"+event.getChannel().getId())){
                matches.signIn(event,indexOfMatch);
            } else if (event.getComponentId().equalsIgnoreCase("reserve_"+event.getChannel().getId())){
                matches.signINReserve(event,indexOfMatch);
            }else if (event.getComponentId().equalsIgnoreCase("out_"+event.getChannel().getId())){
                matches.signOut(event,indexOfMatch);
            }
            matches.updateEmbed(event, indexOfMatch);
        }
    }
}

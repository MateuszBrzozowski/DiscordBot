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
    private static Event event;

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getComponentId().equals("newRecrut")) {
            Recruits recrut = RangerBot.getRecruits();
            recrut.newPodanie(event);
        }
        ButtonClickListener.event = RangerBot.getMatches();
        if (ButtonClickListener.event.isActiveMatch(event.getMessage().getId())>=0){
            int indexOfMatch = ButtonClickListener.event.isActiveMatch(event.getMessage().getId());
            event.deferEdit().queue();
            if (event.getComponentId().equalsIgnoreCase("in_"+event.getMessage().getId())){
                ButtonClickListener.event.signIn(event,indexOfMatch);
            } else if (event.getComponentId().equalsIgnoreCase("reserve_"+event.getMessage().getId())){
                ButtonClickListener.event.signINReserve(event,indexOfMatch);
            }else if (event.getComponentId().equalsIgnoreCase("out_"+event.getMessage().getId())){
                ButtonClickListener.event.signOut(event,indexOfMatch);
            }
            ButtonClickListener.event.updateEmbed(event, indexOfMatch);
        }
    }
}

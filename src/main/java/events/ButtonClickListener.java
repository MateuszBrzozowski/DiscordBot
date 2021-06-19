package events;

import ranger.RangerBot;
import model.Recruits;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ButtonClickListener extends ListenerAdapter {

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getComponentId().equals("newRecrut")) {
            Recruits recrut = RangerBot.getRecruits();
            recrut.newPodanie(event);
//            recrut.createChannelForNewRecrut(event,userName,userID);
        } else if (event.getComponentId().equals("contact")) {
            //Narazie pomijamy
            //System.out.println("Kontakt");
        }
    }
}

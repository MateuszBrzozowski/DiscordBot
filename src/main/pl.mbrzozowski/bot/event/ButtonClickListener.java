package bot.event;

import recrut.Recruits;
import event.Event;
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
        this.event = RangerBot.getMatches();
        if (this.event.isActiveMatch(event.getMessage().getId()) >= 0) {
            int indexOfMatch = this.event.isActiveMatch(event.getMessage().getId());
            event.deferEdit().queue();
            if (event.getComponentId().equalsIgnoreCase("in_" + event.getMessage().getId())) {
                this.event.signIn(event, indexOfMatch);
            } else if (event.getComponentId().equalsIgnoreCase("reserve_" + event.getMessage().getId())) {
                this.event.signINReserve(event, indexOfMatch);
            } else if (event.getComponentId().equalsIgnoreCase("out_" + event.getMessage().getId())) {
                this.event.signOut(event, indexOfMatch);
            }
            this.event.updateEmbed(event, indexOfMatch);
        }
    }
}

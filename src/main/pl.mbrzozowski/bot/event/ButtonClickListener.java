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
        Recruits recrut = RangerBot.getRecruits();
        if (event.getComponentId().equalsIgnoreCase("newRecrut")) {
            recrut.newPodanie(event);
        } else if (event.getComponentId().equalsIgnoreCase("recrutY")) {
            event.deferEdit().queue();
            recrut.confirm(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        } else if (event.getComponentId().equalsIgnoreCase("recrutN")) {
            event.deferEdit().queue();
            recrut.cancel(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        }

        this.event = RangerBot.getEvents();
        if (this.event.getIndexActiveEvent(event.getMessage().getId()) >= 0) {
            int indexOfMatch = this.event.getIndexActiveEvent(event.getMessage().getId());
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

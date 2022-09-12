package pl.mbrzozowski.ranger.bot.events;

import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.recruit.RecruitOpinions;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equalsIgnoreCase(ComponentId.RECRUIT_OPINION_MODAL)) {
            RecruitOpinions recruitOpinions = new RecruitOpinions();
            recruitOpinions.submitForm(event);
        }
    }
}

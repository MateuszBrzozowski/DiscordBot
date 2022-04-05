package bot.event;

import event.ButtonClickType;
import event.Event;
import helpers.RoleID;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import questionnaire.Questionnaires;
import ranger.RangerBot;
import ranger.Repository;
import recrut.Recruits;
import server.service.ServerService;

public class ButtonClickListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private final Event events = Repository.getEvent();

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Recruits recrut = Repository.getRecruits();
        Questionnaires questionnaires = Repository.getQuestionnaires();
        ServerService serverService = Repository.getServerService();
        int indexOfMatch = events.getIndexActiveEvent(event.getMessage().getId());
        boolean isIDCorrect = true;

        if (indexOfMatch >= 0) {
            eventsButtonClick(event, indexOfMatch);
        } else if (event.getComponentId().equalsIgnoreCase("newRecrut")) {
            recrut.newPodanie(event);
        } else if (event.getComponentId().equalsIgnoreCase("recrutY")) {
            recrut.confirm(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        } else if (event.getComponentId().equalsIgnoreCase("recrutN")) {
            recrut.cancel(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        } else if (event.getComponentId().equalsIgnoreCase("end_" + event.getMessage().getId())) {
            questionnaires.end(event.getMessage().getId(), event.getChannel().getId(), event.getUser().getId());
        } else if (event.getComponentId().equalsIgnoreCase("Report")) {
            serverService.buttonClick(event, ButtonClickType.REPORT);
        } else if (event.getComponentId().equalsIgnoreCase("Unban")) {
            serverService.buttonClick(event, ButtonClickType.UNBAN);
        } else if (event.getComponentId().equalsIgnoreCase("Contact")) {
            serverService.buttonClick(event, ButtonClickType.CONTACT);
        } else if (event.getComponentId().equalsIgnoreCase("seedrole")) {
            RoleEditor roleEditor = new RoleEditor();
            roleEditor.addRemoveRole(event.getUser().getId(), RoleID.SEED_ID);
        } else {
            isIDCorrect = false;
        }

        if (isIDCorrect){
            event.deferEdit().queue();
        }
    }

    private void eventsButtonClick(@NotNull ButtonClickEvent event, int indexOfMatch) {
        if (event.getComponentId().equalsIgnoreCase("in_" + event.getMessage().getId())) {
            events.buttonClick(event, indexOfMatch, ButtonClickType.SIGN_IN);
        } else if (event.getComponentId().equalsIgnoreCase("reserve_" + event.getMessage().getId())) {
            events.buttonClick(event, indexOfMatch, ButtonClickType.SIGN_IN_RESERVE);
        } else if (event.getComponentId().equalsIgnoreCase("out_" + event.getMessage().getId())) {
            events.buttonClick(event, indexOfMatch, ButtonClickType.SIGN_OUT);
        }
        events.updateEmbed(indexOfMatch);
    }
}

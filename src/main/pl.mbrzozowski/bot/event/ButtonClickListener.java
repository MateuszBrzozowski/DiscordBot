package bot.event;

import embed.EmbedInfo;
import event.ButtonClickType;
import event.Event;
import helpers.Commands;
import helpers.RoleID;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import questionnaire.Questionnaires;
import ranger.RangerBot;
import ranger.Repository;
import recrut.Recruits;
import recrut.RecruitOpinions;
import server.service.ServerService;
import stats.ServerStats;

public class ButtonClickListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private final Event events = Repository.getEvent();

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Recruits recrut = Repository.getRecruits();
        Questionnaires questionnaires = Repository.getQuestionnaires();
        ServerService serverService = Repository.getServerService();
        ServerStats serverStats = Repository.getServerStats();
        int indexOfMatch = events.getIndexActiveEvent(event.getMessage().getId());
        boolean isIDCorrect = true;

        if (indexOfMatch >= 0) {
            eventsButtonClick(event, indexOfMatch);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.NEW_RECRUT)) {
            recrut.newPodanie(event);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.NEW_RECRUT_CONFIRM)) {
            recrut.confirm(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        } else if (event.getComponentId().equalsIgnoreCase(Commands.NEW_RECRUT_DISCARD)) {
            recrut.cancel(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        } else if (event.getComponentId().equalsIgnoreCase(Commands.QUESTIONNAIRE_END + event.getMessage().getId())) {
            questionnaires.end(event.getMessage().getId(), event.getChannel().getId(), event.getUser().getId());
        } else if (event.getComponentId().equalsIgnoreCase(Commands.SERVER_SERVICE_REPORT)) {
            serverService.buttonClick(event, ButtonClickType.REPORT);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.SERVER_SERVICE_UNBAN)) {
            serverService.buttonClick(event, ButtonClickType.UNBAN);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.SERVER_SERVICE_CONTACT)) {
            serverService.buttonClick(event, ButtonClickType.CONTACT);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.CLOSE)) {
            EmbedInfo.confirmCloseChannel(event.getTextChannel());
        } else if (event.getComponentId().equalsIgnoreCase(Commands.CLOSE_YES)) {
            serverService.closeChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.CLOSE_NO)) {
            event.getMessage().delete().queue();
        } else if (event.getComponentId().equalsIgnoreCase(Commands.REMOVE)) {
            serverService.disableButtons(event.getChannel().getId(), event.getMessageId());
            EmbedInfo.confirmRemoveChannel(event.getTextChannel());
        } else if (event.getComponentId().equalsIgnoreCase(Commands.REMOVE_YES)) {
            serverService.removeChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.REMOVE_NO)) {
            event.getMessage().delete().queue();
        } else if (event.getComponentId().equalsIgnoreCase(Commands.SEED_ROLE)) {
            RoleEditor roleEditor = new RoleEditor();
            roleEditor.addRemoveRole(event.getUser().getId(), RoleID.SEED_ID);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.REFRESH_MAP_STATS)) {
            serverStats.refreshMapStats();
        } else if (event.getComponentId().equalsIgnoreCase(Commands.OPEN_FORM)) {
            isIDCorrect = false;
            RecruitOpinions recrutOpinions = new RecruitOpinions();
            recrutOpinions.openForm(event);
        } else {
            isIDCorrect = false;
        }

        if (isIDCorrect) {
            event.deferEdit().queue();
        }
    }

    private void eventsButtonClick(@NotNull ButtonInteractionEvent event, int indexOfMatch) {
        if (event.getComponentId().equalsIgnoreCase(Commands.EVENTS_SIGN_IN + event.getMessage().getId())) {
            events.buttonClick(event, indexOfMatch, ButtonClickType.SIGN_IN);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.EVENTS_SIGN_IN_RESERVE + event.getMessage().getId())) {
            events.buttonClick(event, indexOfMatch, ButtonClickType.SIGN_IN_RESERVE);
        } else if (event.getComponentId().equalsIgnoreCase(Commands.EVENTS_SIGN_OUT + event.getMessage().getId())) {
            events.buttonClick(event, indexOfMatch, ButtonClickType.SIGN_OUT);
        }
        events.updateEmbed(indexOfMatch);
    }
}

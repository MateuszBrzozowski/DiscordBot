package ranger.bot.events;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ranger.Repository;
import ranger.embed.EmbedInfo;
import ranger.event.ButtonClickType;
import ranger.event.Event;
import ranger.event.EventService;
import ranger.event.EventsGeneratorModel;
import ranger.helpers.*;
import ranger.questionnaire.Questionnaires;
import ranger.recruit.RecruitOpinions;
import ranger.recruit.RecruitsService;
import ranger.server.service.ServerService;
import ranger.stats.ServerStats;

import java.util.Optional;

@Service
public class ButtonClickListener extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;

    @Autowired
    public ButtonClickListener(EventService events, RecruitsService recruitsService) {
        this.eventService = events;
        this.recruitsService = recruitsService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        EventsGeneratorModel eventsGenerator = Repository.getEventsGeneratorModel();
        int indexOfGenerator = eventsGenerator.userHaveActiveGenerator(event.getUser().getId());
        Questionnaires questionnaires = Repository.getQuestionnaires();
        ServerService serverService = Repository.getServerService();
        ServerStats serverStats = Repository.getServerStats();
        boolean isIDCorrect = true;
        boolean isRadaKlanu = Users.hasUserRole(event.getUser().getId(), RoleID.RADA_KLANU);

        Optional<Event> eventOptional = eventService.findEventByMsgId(event.getMessage().getId());

        if (eventOptional.isPresent()) {
            eventsButtonClick(event, eventOptional.get());
            isIDCorrect = false;
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT)) {
            recruitsService.newPodanie(event);
            isIDCorrect = false;
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT_CONFIRM)) {
            recruitsService.confirm(event);
            isIDCorrect = false;
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.QUESTIONNAIRE_END + event.getMessage().getId())) {
            questionnaires.end(event.getMessage().getId(), event.getChannel().getId(), event.getUser().getId());
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_REPORT)) {
            serverService.buttonClick(event, ButtonClickType.REPORT);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_UNBAN)) {
            serverService.buttonClick(event, ButtonClickType.UNBAN);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_CONTACT)) {
            serverService.buttonClick(event, ButtonClickType.CONTACT);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE)) {
            EmbedInfo.confirmCloseChannel(event.getTextChannel());
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES)) {
            serverService.closeChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_NO)) {
            event.getMessage().delete().queue();
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE)) {
            String parentCategoryId = event.getTextChannel().getParentCategoryId();
            if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                if (!isRadaKlanu) {
                    event.deferEdit().queue();
                    return;
                }
            }
            ComponentService.disableButtons(event.getChannel().getId(), event.getMessageId());
            EmbedInfo.confirmRemoveChannel(event.getTextChannel());
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_YES)) {
            ComponentService componentService = new ComponentService(recruitsService);
            componentService.removeChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_NO)) {
            event.getMessage().delete().queue();
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SEED_ROLE)) {
            RoleEditor roleEditor = new RoleEditor();
            roleEditor.addRemoveRole(event.getUser().getId(), RoleID.SEED_ID);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REFRESH_MAP_STATS)) {
            serverStats.refreshMapStats();
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.OPEN_FORM)) {
            isIDCorrect = false;
            RecruitOpinions recrutOpinions = new RecruitOpinions();
            recrutOpinions.openForm(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_IN) && isRadaKlanu) {
            recruitsService.accepted(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_CLOSE_CHANNEL) && isRadaKlanu) {
            recruitsService.closeChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_POSITIVE) && isRadaKlanu) {
            if (!recruitsService.isResult(event.getTextChannel())) {
                EmbedInfo.endPositive(event.getUser().getId(), event.getTextChannel());
                recruitsService.positiveResult(event.getTextChannel());
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NEGATIVE) && isRadaKlanu) {
            if (!recruitsService.isResult(event.getTextChannel())) {
                EmbedInfo.endNegative(event.getUser().getId(), event.getTextChannel());
                recruitsService.negativeResult(event.getTextChannel());
            }
        } else if (indexOfGenerator >= 0) {
            eventsGenerator.saveAnswerAndNextStage(event, indexOfGenerator);
        } else {
            isIDCorrect = false;
        }

        if (isIDCorrect || (!isIDCorrect && !isRadaKlanu)) {
            event.deferEdit().queue();
        }
    }

    private void eventsButtonClick(@NotNull ButtonInteractionEvent event, @NotNull Event eventOptional) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN + event.getMessage().getId())) {
            eventService.buttonClick(event, eventOptional, ButtonClickType.SIGN_IN);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN_RESERVE + event.getMessage().getId())) {
            eventService.buttonClick(event, eventOptional, ButtonClickType.SIGN_IN_RESERVE);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_OUT + event.getMessage().getId())) {
            eventService.buttonClick(event, eventOptional, ButtonClickType.SIGN_OUT);
        }
    }
}

package pl.mbrzozowski.ranger.bot.events;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.event.ButtonClickType;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.recruit.RecruitOpinions;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.server.service.ServerService;

import java.util.Optional;

@Service
public class ButtonClickListener extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;
    private final ServerService serverService;
    private final EventsGeneratorService eventsGeneratorService;

    @Autowired
    public ButtonClickListener(EventService events,
                               RecruitsService recruitsService,
                               ServerService serverService,
                               EventsGeneratorService eventsGeneratorService) {
        this.eventService = events;
        this.recruitsService = recruitsService;
        this.serverService = serverService;
        this.eventsGeneratorService = eventsGeneratorService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent interactionEvent) {
        int indexOfGenerator = eventsGeneratorService.userHaveActiveGenerator(interactionEvent.getUser().getId());
        boolean isIDCorrect = true;
        boolean isRadaKlanu = Users.hasUserRole(interactionEvent.getUser().getId(), RoleID.RADA_KLANU);

        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT)) {
            recruitsService.newPodanie(interactionEvent);
            isIDCorrect = false;
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT_CONFIRM)) {
            recruitsService.confirm(interactionEvent);
            isIDCorrect = false;
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_REPORT)) {
            serverService.buttonClick(interactionEvent, ButtonClickType.REPORT);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_UNBAN)) {
            serverService.buttonClick(interactionEvent, ButtonClickType.UNBAN);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_CONTACT)) {
            serverService.buttonClick(interactionEvent, ButtonClickType.CONTACT);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.CLOSE)) {
            EmbedInfo.confirmCloseChannel(interactionEvent.getTextChannel());
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES)) {
            serverService.closeChannel(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_NO)) {
            interactionEvent.getMessage().delete().queue();
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE)) {
            String parentCategoryId = interactionEvent.getTextChannel().getParentCategoryId();
            if (parentCategoryId != null && parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                if (!isRadaKlanu) {
                    interactionEvent.deferEdit().queue();
                    return;
                }
            }
            ComponentService.disableButtons(interactionEvent.getChannel().getId(), interactionEvent.getMessageId());
            EmbedInfo.confirmRemoveChannel(interactionEvent.getTextChannel());
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_YES)) {
            ComponentService componentService = new ComponentService(recruitsService, serverService);
            componentService.removeChannel(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_NO)) {
            interactionEvent.getMessage().delete().queue();
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SEED_ROLE)) {
            RoleEditor roleEditor = new RoleEditor();
            roleEditor.addRemoveRole(interactionEvent.getUser().getId(), RoleID.SEED_ID);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.OPEN_FORM)) {
            isIDCorrect = false;
            RecruitOpinions recrutOpinions = new RecruitOpinions();
            recrutOpinions.openForm(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_IN) && isRadaKlanu) {
            if (!recruitsService.accepted(interactionEvent)) {
                ResponseMessage.operationNotPossible(interactionEvent);
                isIDCorrect = false;
            }
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_CLOSE_CHANNEL) && isRadaKlanu) {
            recruitsService.closeChannel(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_POSITIVE) && isRadaKlanu) {
            if (!recruitsService.positiveResult(interactionEvent.getUser().getId(), interactionEvent.getTextChannel())) {
                ResponseMessage.operationNotPossible(interactionEvent);
                isIDCorrect = false;
            }
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NEGATIVE) && isRadaKlanu) {
            if (!recruitsService.negativeResult(interactionEvent.getUser().getId(), interactionEvent.getTextChannel())) {
                ResponseMessage.operationNotPossible(interactionEvent);
                isIDCorrect = false;
            }
        } else if (indexOfGenerator >= 0) {
            eventsGeneratorService.saveAnswerAndNextStage(interactionEvent, indexOfGenerator);
        } else {
            isIDCorrect = false;
        }

        Optional<Event> eventOptional = eventService.findEventByMsgId(interactionEvent.getMessage().getId());
        if (eventOptional.isPresent()) {
            eventsButtonClick(interactionEvent, eventOptional.get());
            isIDCorrect = false;
        }

        if (isIDCorrect || (!isIDCorrect && !isRadaKlanu)) {
            interactionEvent.deferEdit().queue();
        }
    }

    private void eventsButtonClick(@NotNull ButtonInteractionEvent interactionEvent, @NotNull Event event) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN + interactionEvent.getMessage().getId())) {
            eventService.buttonClick(interactionEvent, event, ButtonClickType.SIGN_IN);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN_RESERVE + interactionEvent.getMessage().getId())) {
            eventService.buttonClick(interactionEvent, event, ButtonClickType.SIGN_IN_RESERVE);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_OUT + interactionEvent.getMessage().getId())) {
            eventService.buttonClick(interactionEvent, event, ButtonClickType.SIGN_OUT);
        }
    }
}

package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.ButtonClickType;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.recruit.RecruitOpinions;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.server.service.ServerService;

@Slf4j
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
        log.info(interactionEvent.getUser() + " - Button interaction event");
        boolean isRadaKlanu = Users.hasUserRole(interactionEvent.getUser().getId(), RoleID.RADA_KLANU);
        newRecruit(interactionEvent, isRadaKlanu);
    }

    private void newRecruit(@NotNull ButtonInteractionEvent interactionEvent, boolean isRadaKlanu) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT)) {
            recruitsService.newPodanie(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT_CONFIRM)) {
            recruitsService.confirm(interactionEvent);
        } else {
            recruitChannelReaction(interactionEvent, isRadaKlanu);
        }
    }

    private void recruitChannelReaction(@NotNull ButtonInteractionEvent interactionEvent, boolean isRadaKlanu) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_ACCEPTED)) {
            if (isRadaKlanu) {
                recruitsService.accepted(interactionEvent);
            } else {
                ResponseMessage.noPermission(interactionEvent);
            }
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NOT_ACCEPTED)) {
            if (isRadaKlanu) {
                recruitsService.recruitNotAccepted(interactionEvent);
            } else {
                ResponseMessage.noPermission(interactionEvent);
            }
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_POSITIVE)) {
            if (isRadaKlanu) {
                if (!recruitsService.positiveResult(interactionEvent)) {
                    ResponseMessage.operationNotPossible(interactionEvent);
                }
            } else {
                ResponseMessage.noPermission(interactionEvent);
            }
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NEGATIVE)) {
            if (isRadaKlanu) {
                if (!recruitsService.negativeResult(interactionEvent)) {
                    ResponseMessage.operationNotPossible(interactionEvent);
                }
            } else {
                ResponseMessage.noPermission(interactionEvent);
            }
        } else {
            serverServiceReport(interactionEvent, isRadaKlanu);
        }
    }

    private void serverServiceReport(@NotNull ButtonInteractionEvent interactionEvent, boolean isRadaKlanu) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_REPORT)) {
            serverService.buttonClick(interactionEvent, ButtonClickType.REPORT);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_UNBAN)) {
            serverService.buttonClick(interactionEvent, ButtonClickType.UNBAN);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_CONTACT)) {
            serverService.buttonClick(interactionEvent, ButtonClickType.CONTACT);
        } else {
            serverServiceCloseChannel(interactionEvent, isRadaKlanu);
        }
    }

    private void serverServiceCloseChannel(@NotNull ButtonInteractionEvent interactionEvent, boolean isRadaKlanu) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.CLOSE)) {
            interactionEvent.deferEdit().queue();
            EmbedInfo.confirmCloseChannel(interactionEvent.getChannel().asTextChannel());
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES)) {
            interactionEvent.getMessage().delete().queue();
            serverService.closeChannel(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_NO)) {
            interactionEvent.getMessage().delete().queue();
        } else {
            removeChannelsButtons(interactionEvent, isRadaKlanu);
        }
    }

    private void removeChannelsButtons(@NotNull ButtonInteractionEvent interactionEvent, boolean isRadaKlanu) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_RECRUIT_CHANNEL)) {
            if (isRadaKlanu) {
                interactionEvent.getMessage().delete().queue();
                EmbedInfo.confirmRemoveChannel(interactionEvent.getChannel().asTextChannel());
            } else {
                ResponseMessage.noPermission(interactionEvent);
            }
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_SERVER_SERVICE_CHANNEL)) {
            interactionEvent.getMessage().delete().queue();
            EmbedInfo.confirmRemoveChannel(interactionEvent.getChannel().asTextChannel());
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_YES)) {
            removeChannelDB(interactionEvent, isRadaKlanu);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_NO)) {
            interactionEvent.deferEdit().queue();
        } else {
            eventsButtons(interactionEvent);
        }
    }

    private void eventsButtons(@NotNull ButtonInteractionEvent interactionEvent) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN + interactionEvent.getMessage().getId())) {
            eventService.buttonClick(interactionEvent, ButtonClickType.SIGN_IN);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN_RESERVE + interactionEvent.getMessage().getId())) {
            eventService.buttonClick(interactionEvent, ButtonClickType.SIGN_IN_RESERVE);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_OUT + interactionEvent.getMessage().getId())) {
            eventService.buttonClick(interactionEvent, ButtonClickType.SIGN_OUT);
        } else {
            openForm(interactionEvent);
        }
    }

    private void openForm(@NotNull ButtonInteractionEvent interactionEvent) {
        if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.OPEN_FORM_RECRUIT_OPINION)) {
            RecruitOpinions.openOpinionAboutRecruit(interactionEvent);
        } else if (interactionEvent.getComponentId().equalsIgnoreCase(ComponentId.OPEN_FORM_ANONYMOUS_COMPLAINTS)) {
            boolean isClanMember = Users.hasUserRole(interactionEvent.getUser().getId(), RoleID.CLAN_MEMBER_ID);
            if (isClanMember) {
                ResponseMessage.operationNotPossible(interactionEvent);
            } else {
                RecruitOpinions.openAnonymousComplaints(interactionEvent);
            }
        } else {
            eventsGenerator(interactionEvent);
        }
    }

    private void eventsGenerator(@NotNull ButtonInteractionEvent interactionEvent) {
        int indexOfGenerator = eventsGeneratorService.userHaveActiveGenerator(interactionEvent.getUser().getId());
        if (indexOfGenerator >= 0) {
            interactionEvent.deferEdit().queue();
            eventsGeneratorService.saveAnswerAndNextStage(interactionEvent, indexOfGenerator);
        }
    }

    private void removeChannelDB(@NotNull ButtonInteractionEvent interactionEvent, boolean isRadaKlanu) {
        String parentCategoryId = interactionEvent.getChannel().asTextChannel().getParentCategoryId();
        if (parentCategoryId != null) {
            if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID) && isRadaKlanu) {
                recruitsService.deleteChannelByID(interactionEvent.getChannel().getId());
                ComponentService.removeChannel(interactionEvent);
            } else if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_SERVER)) {
                serverService.removeChannel(interactionEvent);
                ComponentService.removeChannel(interactionEvent);
            } else {
                ResponseMessage.noPermission(interactionEvent);
            }
        }
    }
}

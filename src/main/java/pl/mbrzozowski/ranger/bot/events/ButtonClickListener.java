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
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        log.info("{} - Button interaction event button(Label={}), channel(channelId={}, channelName={})",
                event.getUser(),
                event.getButton().getLabel(),
                event.getChannel().getId(),
                event.getChannel().getName());
        boolean isRadaKlanu = Users.hasUserRole(event.getUser().getId(), RoleID.RADA_KLANU);
        newRecruit(event, isRadaKlanu);
    }

    private void newRecruit(@NotNull ButtonInteractionEvent event, boolean isRadaKlanu) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT)) {
            recruitsService.newPodanie(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUT_CONFIRM)) {
            recruitsService.confirm(event);
        } else {
            recruitChannelReaction(event, isRadaKlanu);
        }
    }

    private void recruitChannelReaction(@NotNull ButtonInteractionEvent event, boolean isRadaKlanu) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_ACCEPTED)) {
            if (isRadaKlanu) {
                recruitsService.accepted(event);
            } else {
                ResponseMessage.noPermission(event);
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NOT_ACCEPTED)) {
            if (isRadaKlanu) {
                recruitsService.recruitNotAccepted(event);
            } else {
                ResponseMessage.noPermission(event);
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_POSITIVE)) {
            if (isRadaKlanu) {
                if (!recruitsService.positiveResult(event)) {
                    ResponseMessage.operationNotPossible(event);
                }
            } else {
                ResponseMessage.noPermission(event);
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NEGATIVE)) {
            if (isRadaKlanu) {
                if (!recruitsService.negativeResult(event)) {
                    ResponseMessage.operationNotPossible(event);
                }
            } else {
                ResponseMessage.noPermission(event);
            }
        } else {
            serverServiceReport(event, isRadaKlanu);
        }
    }

    private void serverServiceReport(@NotNull ButtonInteractionEvent event, boolean isRadaKlanu) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_REPORT)) {
            serverService.buttonClick(event, ButtonClickType.REPORT);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_UNBAN)) {
            serverService.buttonClick(event, ButtonClickType.UNBAN);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_CONTACT)) {
            serverService.buttonClick(event, ButtonClickType.CONTACT);
        } else {
            serverServiceCloseChannel(event, isRadaKlanu);
        }
    }

    private void serverServiceCloseChannel(@NotNull ButtonInteractionEvent event, boolean isRadaKlanu) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE)) {
            EmbedInfo.confirmCloseChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES) ||
                event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_NO)) {
            deleteMessageAfterButtonInteraction(event);
            if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES)) {
                serverService.closeChannel(event);
            }
        } else {
            removeChannelsButtons(event, isRadaKlanu);
        }
    }

    private void removeChannelsButtons(@NotNull ButtonInteractionEvent event, boolean isRadaKlanu) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_RECRUIT_CHANNEL)) {
            if (isRadaKlanu) {
                deleteMessageAfterButtonInteraction(event);
                EmbedInfo.confirmRemoveChannel(event.getChannel().asTextChannel());
            } else {
                ResponseMessage.noPermission(event);
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_SERVER_SERVICE_CHANNEL)) {
            deleteMessageAfterButtonInteraction(event);
            EmbedInfo.confirmRemoveChannel(event.getChannel().asTextChannel());
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_YES)) {
            removeChannelDB(event, isRadaKlanu);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_NO)) {
            event.deferEdit().queue();
        } else {
            eventsButtons(event);
        }
    }

    private void eventsButtons(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN + event.getMessage().getId())) {
            eventService.buttonClick(event, ButtonClickType.SIGN_IN);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_IN_RESERVE + event.getMessage().getId())) {
            eventService.buttonClick(event, ButtonClickType.SIGN_IN_RESERVE);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENTS_SIGN_OUT + event.getMessage().getId())) {
            eventService.buttonClick(event, ButtonClickType.SIGN_OUT);
        } else {
            openForm(event);
        }
    }

    private void openForm(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.OPEN_FORM_RECRUIT_OPINION)) {
            RecruitOpinions.openOpinionAboutRecruit(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.OPEN_FORM_ANONYMOUS_COMPLAINTS)) {
            boolean isClanMember = Users.hasUserRole(event.getUser().getId(), RoleID.CLAN_MEMBER_ID);
            if (isClanMember) {
                ResponseMessage.operationNotPossible(event);
            } else {
                RecruitOpinions.openAnonymousComplaints(event);
            }
        } else {
            eventsGenerator(event);
        }
    }

    private void eventsGenerator(@NotNull ButtonInteractionEvent event) {
        int indexOfGenerator = eventsGeneratorService.userHaveActiveGenerator(event.getUser().getId());
        if (indexOfGenerator >= 0) {
            event.deferEdit().queue();
            eventsGeneratorService.saveAnswerAndNextStage(event, indexOfGenerator);
        }
    }

    private void removeChannelDB(@NotNull ButtonInteractionEvent event, boolean isRadaKlanu) {
        String parentCategoryId = event.getChannel().asTextChannel().getParentCategoryId();
        if (parentCategoryId != null) {
            if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID) && isRadaKlanu) {
                recruitsService.deleteChannelByID(event.getChannel().getId());
                ComponentService.removeChannel(event);
            } else if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_SERVER)) {
                serverService.removeChannel(event);
                ComponentService.removeChannel(event);
            } else {
                ResponseMessage.noPermission(event);
            }
        }
    }

    private void deleteMessageAfterButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        event.getMessage().delete().queue();
    }
}

package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.ButtonClickType;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.games.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.recruit.RecruitOpinions;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.server.seed.call.SeedCallService;
import pl.mbrzozowski.ranger.server.service.ServerService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ButtonClickListener extends ListenerAdapter {

    private final EventsGeneratorService eventsGeneratorService;
    private final EventsSettingsService eventsSettingsService;
    private final GiveawayService giveawayService;
    private final RecruitsService recruitsService;
    private final SeedCallService seedCallService;
    private final ServerService serverService;
    private final EventService eventService;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        log.info("{} - Button interaction event button(Label={}), channel(channelId={}, channelName={})",
                event.getUser(),
                event.getButton().getLabel(),
                event.getChannel().getId(),
                event.getChannel().getName());
        boolean isAdmin = Users.isAdmin(event.getUser().getId());
        newRecruit(event, isAdmin);
    }

    private void newRecruit(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.NEW_RECRUIT)) {
            recruitsService.newPodanie(event);
        } else if (event.getComponentId().equals(ComponentId.FORM_SEND)) {
            recruitsService.confirmYesNo(event);
        } else if (event.getComponentId().equals(ComponentId.FORM_SEND_NO)) {
            recruitsService.confirmMessageBack(event);
        } else if (event.getComponentId().length() > ComponentId.CONFIRM_FORM_SEND.length() &&
                event.getComponentId().startsWith(ComponentId.CONFIRM_FORM_SEND)) {
            recruitsService.confirm(event);
        } else if (event.getComponentId().length() > ComponentId.CONFIRM_FORM_RECEIVED.length() &&
                event.getComponentId().startsWith(ComponentId.CONFIRM_FORM_RECEIVED)) {
            recruitsService.confirmFormReceived(event);
        } else if (event.getComponentId().length() > ComponentId.DECLINE_FORM_SEND.length() &&
                event.getComponentId().startsWith(ComponentId.DECLINE_FORM_SEND)) {
            recruitsService.declineForm(event);
        } else {
            recruitChannelReaction(event, isAdmin);
        }
    }

    private void recruitChannelReaction(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_ACCEPTED)) {
            recruitsService.accepted(event, isAdmin);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NOT_ACCEPTED)) {
            recruitsService.recruitNotAccepted(event, isAdmin);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_POSITIVE)) {
            recruitsService.positiveResult(event, isAdmin);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.RECRUIT_NEGATIVE)) {
            recruitsService.negativeResult(event, isAdmin);
        } else {
            serverServiceReport(event, isAdmin);
        }
    }

    private void serverServiceReport(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_REPORT)) {
            serverService.buttonClick(event, ButtonClickType.REPORT);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_UNBAN)) {
            serverService.buttonClick(event, ButtonClickType.UNBAN);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_CONTACT)) {
            serverService.buttonClick(event, ButtonClickType.CONTACT);
        } else {
            serverServiceCloseChannel(event, isAdmin);
        }
    }

    private void serverServiceCloseChannel(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE)) {
            EmbedInfo.confirmCloseChannel(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES) ||
                event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_NO)) {
            deleteMessageAfterButtonInteraction(event);
            if (event.getComponentId().equalsIgnoreCase(ComponentId.CLOSE_YES)) {
                serverService.closeChannel(event);
            }
        } else {
            removeChannelsButtons(event, isAdmin);
        }
    }

    private void removeChannelsButtons(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_RECRUIT_CHANNEL)) {
            if (isAdmin) {
                deleteMessageAfterButtonInteraction(event);
                EmbedInfo.confirmRemoveChannel(event.getChannel().asTextChannel());
            } else {
                ResponseMessage.noPermission(event);
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_SERVER_SERVICE_CHANNEL)) {
            deleteMessageAfterButtonInteraction(event);
            EmbedInfo.confirmRemoveChannel(event.getChannel().asTextChannel());
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.SERVER_SERVICE_OPEN_NO_CLOSE)) {
            serverService.openNoClose(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_YES)) {
            removeChannelDB(event, isAdmin);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.REMOVE_NO)) {
            event.deferEdit().queue();
        } else {
            giveawayButtons(event, isAdmin);
        }
    }

    private void giveawayButtons(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        if (isAdmin && (event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_BTN_BACK) ||
                event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_BTN_NEXT) ||
                event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_BTN_CANCEL) ||
                event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_BTN_REMOVE) ||
                event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_BTN_REMOVE_ALL))) {
            event.deferEdit().queue();
            giveawayService.buttonGeneratorEvent(event);
        } else if (event.getComponentId().length() > "giveaway".length() &&
                event.getComponentId().substring(0, "giveaway".length()).equalsIgnoreCase("giveaway")) {
            giveawayService.buttonClickSignIn(event);
        } else if (event.getComponentId().length() > ComponentId.GIVEAWAY_END_SURE_YES.length() &&
                event.getComponentId().substring(0, ComponentId.GIVEAWAY_END_SURE_YES.length()).equalsIgnoreCase(ComponentId.GIVEAWAY_END_SURE_YES)) {
            giveawayService.end(event, event.getComponentId().substring(ComponentId.GIVEAWAY_END_SURE_YES.length()), true);
        } else if (event.getComponentId().length() > ComponentId.GIVEAWAY_CANCEL_SURE_YES.length() &&
                event.getComponentId().substring(0, ComponentId.GIVEAWAY_CANCEL_SURE_YES.length()).equalsIgnoreCase(ComponentId.GIVEAWAY_CANCEL_SURE_YES)) {
            giveawayService.end(event, event.getComponentId().substring(ComponentId.GIVEAWAY_CANCEL_SURE_YES.length()), false);
        } else if (event.getComponentId().length() > ComponentId.GIVEAWAY_RE_ROLL_SURE_YES.length() &&
                event.getComponentId().substring(0, ComponentId.GIVEAWAY_RE_ROLL_SURE_YES.length()).equalsIgnoreCase(ComponentId.GIVEAWAY_CANCEL_SURE_YES)) {
            giveawayService.reRoll(event, event.getComponentId().substring(ComponentId.GIVEAWAY_RE_ROLL_SURE_YES.length()));
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
        if (event.getComponentId().equals(ComponentId.OPEN_FORM_ANONYMOUS_COMPLAINTS)) {
            boolean isClanMember = Users.hasUserRole(event.getUser().getId(), RoleID.CLAN_MEMBER_ID);
            if (isClanMember) {
                ResponseMessage.operationNotPossible(event);
            } else {
                RecruitOpinions.getInstance().openAnonymousComplaints(event);
            }
        } else {
            eventsGenerator(event);
        }
    }

    private void eventsGenerator(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals(ComponentId.EVENT_GENERATOR_BTN_BACK) ||
                event.getComponentId().equals(ComponentId.EVENT_GENERATOR_BTN_NEXT) ||
                event.getComponentId().equals(ComponentId.EVENT_GENERATOR_BTN_CANCEL) ||
                event.getComponentId().equals(ComponentId.EVENT_GENERATOR_MODAL_TITLE) ||
                event.getComponentId().equals(ComponentId.EVENT_GENERATOR_MODAL_TIME)) {
            eventsGeneratorService.buttonEvent(event);
        } else {
            eventSettings(event);
        }
    }

    private void eventSettings(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals(ComponentId.EVENT_SETTINGS_BTN_BACK) ||
                event.getComponentId().equals(ComponentId.EVENT_SETTINGS_BTN_NEXT) ||
                event.getComponentId().equals(ComponentId.EVENT_SETTINGS_BTN_CANCEL) ||
                event.getComponentId().equals(ComponentId.EVENT_SETTINGS_BTN_SAVE) ||
                event.getComponentId().equals(ComponentId.EVENT_SETTINGS_GO_TO_START)) {
            eventsSettingsService.buttonEvent(event);
        } else {
            seedCall(event);
        }
    }

    private void seedCall(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals(ComponentId.SEED_CALL_BACK) ||
                event.getComponentId().equals(ComponentId.SEED_CALL_NEXT)) {
            seedCallService.buttonClickMMessage(event);
        }

    }

    private void removeChannelDB(@NotNull ButtonInteractionEvent event, boolean isAdmin) {
        String parentCategoryId = event.getChannel().asTextChannel().getParentCategoryId();
        if (parentCategoryId != null) {
            if (RangersGuild.compareCategoryId(parentCategoryId, RangersGuild.CategoryId.RECRUIT) && isAdmin) {
                recruitsService.deleteChannelAfterButtonClick(event);
            } else if (RangersGuild.compareCategoryId(parentCategoryId, RangersGuild.CategoryId.SERVER)) {
                serverService.deleteChannelAfterButtonClick(event);
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

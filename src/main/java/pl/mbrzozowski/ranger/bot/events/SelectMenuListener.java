package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventsGeneratorService;
import pl.mbrzozowski.ranger.event.EventsSettingsService;
import pl.mbrzozowski.ranger.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.role.Role;
import pl.mbrzozowski.ranger.role.RoleService;

import java.util.List;

@Slf4j
@Service
public class SelectMenuListener extends ListenerAdapter {

    private final EventsGeneratorService eventsGeneratorService;
    private final RoleService roleService;
    private final GiveawayService giveawayService;
    private final EventsSettingsService eventsSettingsService;

    @Autowired
    public SelectMenuListener(EventsGeneratorService eventsGeneratorService,
                              RoleService roleService,
                              GiveawayService giveawayService,
                              EventsSettingsService eventsSettingsService) {
        this.eventsGeneratorService = eventsGeneratorService;
        this.roleService = roleService;
        this.giveawayService = giveawayService;
        this.eventsSettingsService = eventsSettingsService;
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        log.info("{} - StringSelectInteractionEvent{ComponentId={}} interaction event", event.getUser(), event.getComponentId());
        boolean isRoles = event.getComponentId().equalsIgnoreCase(ComponentId.ROLES);
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        if (isRoles) {
            List<ActionRow> actionRows = event.getMessage().getActionRows();
            event.getInteraction().deferEdit().queue();
            event.getMessage()
                    .editMessageEmbeds(event.getMessage().getEmbeds().get(0))
                    .setActionRow(actionRows.get(0).getActionComponents())
                    .queue();
            String roleID = selectedOptions.get(0).getValue();
            RoleEditor roleEditor = new RoleEditor();

            List<Role> roleList = roleService.findAll();

            for (Role role : roleList) {
                if (roleID.equalsIgnoreCase(role.getDiscordId())) {
                    roleEditor.addRemoveRole(event.getUser().getId(), role.getDiscordId());
                }
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_SELECT_MENU)) {
            giveawayService.selectAnswer(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENT_GENERATOR_SELECT_MENU_PERM)) {
            eventsGeneratorService.selectAnswer(event);
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.EVENT_SETTINGS_SELECT_MENU)) {
            eventsSettingsService.selectAnswer(event);
        }
    }
}

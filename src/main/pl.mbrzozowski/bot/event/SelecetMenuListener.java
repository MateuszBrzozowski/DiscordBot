package bot.event;

import event.EventsGeneratorModel;
import helpers.ComponentId;
import helpers.RoleID;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;

import java.util.List;

public class SelecetMenuListener extends ListenerAdapter {

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        EventsGeneratorModel eventsGenerator = Repository.getEventsGeneratorModel();
        int indexOfGenerator = eventsGenerator.userHaveActiveGenerator(event.getUser().getId());
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

            if (roleID.equalsIgnoreCase(RoleID.TARKOV)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.TARKOV);
            } else if (roleID.equalsIgnoreCase(RoleID.VIRTUAL_REALITY)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.VIRTUAL_REALITY);
            } else if (roleID.equalsIgnoreCase(RoleID.SQUAD)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.SQUAD);
            } else if (roleID.equalsIgnoreCase(RoleID.CS)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.CS);
            } else if (roleID.equalsIgnoreCase(RoleID.WAR_THUNDER)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.WAR_THUNDER);
            } else if (roleID.equalsIgnoreCase(RoleID.MINECRAFT)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.MINECRAFT);
            } else if (roleID.equalsIgnoreCase(RoleID.RAINBOW_SIX)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.RAINBOW_SIX);
            } else if (roleID.equalsIgnoreCase(RoleID.WARGAME)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.WARGAME);
            } else if (roleID.equalsIgnoreCase(RoleID.ARMA)) {
                roleEditor.addRemoveRole(event.getUser().getId(), RoleID.ARMA);
            }
        } else if (event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_FINISH_SELECT_MENU) && indexOfGenerator >= 0) {
            event.getInteraction().deferEdit().queue();
            eventsGenerator.saveAnswerAndNextStage(event, indexOfGenerator);
        }
    }
}

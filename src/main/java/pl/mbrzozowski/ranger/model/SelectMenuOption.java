package pl.mbrzozowski.ranger.model;

import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectMenuOption {

    List<SelectOption> selectOptions;

    public List<SelectOption> getOptions(@NotNull String... labels) {
        selectOptions = new ArrayList<>();
        int option = 0;
        if (labels.length > SelectMenu.OPTIONS_MAX_AMOUNT) {
            throw new IllegalArgumentException("Max amount of options");
        }
        for (String label : labels) {
            if (StringUtils.isBlank(label)) {
                throw new IllegalArgumentException("Label can not be blank");
            }
            selectOptions.add(SelectOption.of(label, "SMO_" + option));
            option++;
        }
        return selectOptions;
    }
}

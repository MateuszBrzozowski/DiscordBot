package pl.mbrzozowski.ranger.giveaway;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.exceptions.StageNoSupportedException;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static pl.mbrzozowski.ranger.giveaway.GiveawayGeneratorStage.*;
import static pl.mbrzozowski.ranger.helpers.ComponentId.*;

public class GiveawayGenerator {

    private final User user;
    private Message message;
    private String selectMenuValue;
    private GiveawayGeneratorStage stage = TIME_MODE;


    public GiveawayGenerator(User user) {
        this.user = user;
        start();
    }

    private void start() {
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(getEmbed())
                .setComponents(
                        ActionRow.of(getSelectMenu()),
                        ActionRow.of(getButtons())
                )
                .queue(message -> this.message = message));
    }

    @NotNull
    private MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Giveaway - Generator");
        builder.setColor(Color.YELLOW);
        switch (stage) {
            case TIME_MODE -> builder.setDescription("Wybierz w jaki sposób chcesz określić czas zakończenia");
            case TIME_MODE_NOT_SELECTED -> {
                builder.setDescription("Wybierz w jaki sposób chcesz określić czas zakończenia");
                builder.addField("BŁĄD: Nie wybrano sposobu określenia czasu zakończenia", "", false);
                builder.setColor(Color.RED);
            }
            case DATE_SELECT -> builder.setDescription("Wybierz date zakończenia");
            case TIME_SELECT -> builder.setDescription("Wybierz czas zakończenia");
            case TIME_DURATION -> builder.setDescription("Wybierz jak długo ma trwać giveaway");
            case DATE_NOT_SELECTED -> {
                builder.setDescription("Wybierz date i czas zakończenia");
                builder.addField("BŁĄD: Nie wybrano daty", "", false);
                builder.setColor(Color.RED);
            }
            case CANCEL -> builder.setDescription("Przerwano generowanie giveawaya");
            default -> throw new IllegalArgumentException("Incorrect stage - " + stage.name());
        }

        return builder.build();
    }

    @NotNull
    private SelectMenu getSelectMenu() {
        switch (stage) {
            case TIME_MODE, TIME_MODE_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_TIME_MODE_SELECTOR)
                        .setPlaceholder("Wybierz sposób")
                        .addOption(SelectMenuOption.DATE_TIME.getLabel(), SelectMenuOption.DATE_TIME.getValue())
                        .addOption(SelectMenuOption.TIME_DURATION.getLabel(), SelectMenuOption.TIME_DURATION.getValue())
                        .build();
            }
            case TIME_DURATION -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_TIME_DURATION_SELECTOR)
                        .setPlaceholder("Ile ma trwać giveaway?")
                        .addOptions(SelectMenuOption.getDurationTimes())
                        .build();
            }
            case DATE_SELECT, DATE_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_DATE_SELECTOR)
                        .setPlaceholder("Wybierz date zakończenia")
                        .addOptions(SelectMenuOption.getDays())
                        .build();
            }
            case TIME_SELECT -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_TIME_SELECTOR)
                        .setPlaceholder("Wybierz godzinę zakończenia")
                        .addOptions(SelectMenuOption.getHours())
                        .build();
            }
            default -> throw new IllegalArgumentException("Incorrect stage - " + stage.name());
        }

    }

    @NotNull
    private Collection<? extends ItemComponent> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (stage.equals(TIME_MODE)) {
            buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz").asDisabled());
        } else {
            buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz"));
        }
        buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Dalej"));
        buttons.add(Button.danger(GIVEAWAY_GENERATOR_BTN_CANCEL, "Przerwij"));
        return buttons;
    }

    public void selectAnswer(@NotNull StringSelectInteractionEvent event) {
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        selectMenuValue = selectedOptions.get(0).getValue();
    }

    public void cancel() {
        stage = CANCEL;
        message.editMessageEmbeds(getEmbed()).setComponents().queue();
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_BACK)) {
            buttonBack();
        } else if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_NEXT)) {
            buttonNext();
        } else {
            throw new NoSuchElementException("No such button - " + event.getComponentId());
        }
    }

    private void buttonBack() {
        switch (stage) {
            case TIME_MODE -> message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
            case TIME_DURATION, DATE_SELECT -> {
                stage = TIME_MODE;
                selectMenuValue = null;
                buttonBack();
            }
            case TIME_SELECT -> {
                stage = DATE_SELECT;
                selectMenuValue = null;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
            }
            default -> throw new StageNoSupportedException("Stage - " + stage.name());
        }
    }

    private void buttonNext() {
        switch (stage) {
            case TIME_MODE -> {
                if (selectMenuValue == null) {
                    stage = TIME_MODE_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
                    stage = TIME_MODE;
                } else {
                    if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.DATE_TIME.getValue())) {
                        //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                        selectMenuValue = null;
                        stage = DATE_SELECT;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
                    } else if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.TIME_DURATION.getValue())) {
                        //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                        selectMenuValue = null;
                        stage = TIME_DURATION;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
                    }
                }
            }
            case DATE_SELECT -> {
                if (selectMenuValue == null) {
                    stage = DATE_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
                    stage = DATE_SELECT;
                } else {
                    //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                    LocalDateTime date = SelectMenuOption.getDate(selectMenuValue);
                    selectMenuValue = null;
                    stage = TIME_SELECT;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getButtons()), ActionRow.of(getSelectMenu())).queue();
                }
            }
            case TIME_SELECT, TIME_DURATION -> {
                //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                selectMenuValue = null;

            }
            default -> throw new StageNoSupportedException("Stage - " + stage.name());
        }
    }
}

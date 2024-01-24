package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.exceptions.StageNoSupportedException;
import pl.mbrzozowski.ranger.helpers.ComponentId;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.giveaway.GiveawayGeneratorStage.*;
import static pl.mbrzozowski.ranger.helpers.ComponentId.*;

@Slf4j
public class GiveawayGenerator {

    private static final String PRIZE_NAME_ID = "prizeNameId";
    private static final String PRIZE_QTY_ID = "prizeQtyId";
    private static final int MAX_NUMBER_OF_PRIZE = 25;
    private List<Prize> prizes = new ArrayList<>();
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
                builder.addField("", "- Nie wybrano sposobu określenia czasu zakończenia!", false);
                builder.setColor(Color.RED);
            }
            case DATE_SELECT -> builder.setDescription("Wybierz date zakończenia");
            case DATE_NOT_SELECTED -> {
                builder.setDescription("Wybierz date zakończenia");
                builder.addField("", "-: Nie wybrano daty!", false);
                builder.setColor(Color.RED);
            }
            case TIME_SELECT -> builder.setDescription("Wybierz czas zakończenia");
            case TIME_NOT_SELECTED -> {
                builder.setDescription("Wybierz czas zakończenia");
                builder.addField("", "- Nie wybrano czasu!", false);
                builder.setColor(Color.RED);
            }
            case TIME_DURATION -> builder.setDescription("Wybierz jak długo ma trwać giveaway");
            case TIME_DURATION_NOT_SELECTED -> {
                builder.setDescription("Wybierz jak długo ma trwać giveaway");
                builder.addField("", "- Nie wybrano czasu trwania!", false);
                builder.setColor(Color.RED);
            }
            case CLAN_MEMBER_EXCLUDE -> builder.setDescription("Czy wykluczyć **Clan Memberów** z giveawaya?");
            case CLAN_MEMBER_EXCLUDE_NOT_SELECTED -> {
                builder.setDescription("Czy wykluczyć **Clan Memberów** z giveawaya?");
                builder.addField("", "- Nie wybrano odpowiedzi!", false);
                builder.setColor(Color.RED);
            }
            case PRIZE -> {
                builder.setDescription("Ustaw nagrody");
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(), false);
                }
            }
            case PRIZE_QTY_NOT_CORRECT -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("", "- Ilość sztuk musi być liczbą!**\n", false);
                builder.setColor(Color.RED);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(), false);
                }
            }
            case MAX_NUMBER_OF_PRIZE_STAGE -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("", "- Osiągnięto maksymalną ilość nagród dla tego giveawaya!", false);
                builder.setColor(Color.RED);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(), false);
                }
            }
            case PRIZE_REMOVE -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("Usuwanie nagród", "", false);
                builder.setColor(Color.CYAN);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(), false);
                }
            }
            case CANCEL -> builder.setDescription("Przerwano generowanie giveawaya");
            default -> throw new IllegalArgumentException("Incorrect stage - " + stage.name());
        }

        return builder.build();
    }

    @NotNull
    private String getPrizesDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < prizes.size(); i++) {
            stringBuilder
                    .append(i + 1)
                    .append(". ")
                    .append(prizes.get(i).getNumberOfPrizes())
                    .append("x ")
                    .append(prizes.get(i).getName())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    @NotNull
    private String getPrizeDescription(@NotNull Prize prize) {
        return prize.getNumberOfPrizes() +
                "x " +
                prize.getName();
    }

    @NotNull
    private SelectMenu getSelectMenu() {
        switch (stage) {
            case TIME_MODE, TIME_MODE_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_TIME_MODE_SELECTOR)
                        .setPlaceholder("Wybierz sposób")
                        .addOptions(SelectMenuOption.getTimeMode())
                        .build();
            }
            case TIME_DURATION, TIME_DURATION_NOT_SELECTED -> {
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
            case TIME_SELECT, TIME_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_TIME_SELECTOR)
                        .setPlaceholder("Wybierz godzinę zakończenia")
                        .addOptions(SelectMenuOption.getHours())
                        .build();
            }
            case CLAN_MEMBER_EXCLUDE, CLAN_MEMBER_EXCLUDE_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_TIME_SELECTOR)
                        .setPlaceholder("Czy wykluczyć Clan Memberów?")
                        .addOptions(SelectMenuOption.getExclude())
                        .build();
            }
            case PRIZE, PRIZE_QTY_NOT_CORRECT, MAX_NUMBER_OF_PRIZE_STAGE -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_PRIZE_SELECTOR)
                        .setPlaceholder("Wybierz opcję")
                        .addOptions(SelectMenuOption.getPrize())
                        .build();
            }
            case PRIZE_REMOVE -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_PRIZE_SELECTOR)
                        .setPlaceholder("Wybierz nagrodę do usunięcia")
                        .addOptions(getPrizesAsSelectMenuOptions())
                        .build();
            }
            default -> throw new IllegalArgumentException("Incorrect stage - " + stage.name());
        }

    }

    @NotNull
    private Collection<? extends SelectOption> getPrizesAsSelectMenuOptions() {
        List<SelectOption> selectOptions = new ArrayList<>();
        for (int i = 0; i < prizes.size(); i++) {
            selectOptions.add(SelectOption.of(getPrizeDescription(prizes.get(i)), "prize:" + i));
        }
        return selectOptions;
    }

    @NotNull
    private Collection<? extends ItemComponent> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (stage.equals(PRIZE_REMOVE)) {
            buttons.add(Button.primary(GIVEAWAY_GENERATOR_BTN_REMOVE, "Usuń zaznaczony"));
            buttons.add(Button.primary(GIVEAWAY_GENERATOR_BTN_REMOVE_ALL, "Usuń wszystkie"));
            buttons.add(Button.primary(GIVEAWAY_GENERATOR_BTN_BACK, "Powrót"));
        } else {
            if (stage.equals(TIME_MODE)) {
                buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz").asDisabled());
            } else {
                buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz"));
            }
            if (stage.equals(PRIZE)) {
                if (prizes.isEmpty()) {
                    buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Zakończ").asDisabled());
                } else {
                    buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Zakończ"));
                }
            } else {
                buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Dalej"));
            }
        }
        buttons.add(Button.danger(GIVEAWAY_GENERATOR_BTN_CANCEL, "Przerwij"));
        return buttons;
    }

    public void selectAnswer(@NotNull StringSelectInteractionEvent event) {
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        selectMenuValue = selectedOptions.get(0).getValue();
        if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.ADD_PRIZE.getValue())) {
            if (prizes.size() < MAX_NUMBER_OF_PRIZE) {
                showModalToAddPrize(event);
            } else {
                event.getInteraction().deferEdit().queue();
                stage = MAX_NUMBER_OF_PRIZE_STAGE;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                stage = PRIZE;
            }
        } else {
            event.getInteraction().deferEdit().queue();
            if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.REMOVE_PRIZE.getValue())) {
                selectMenuValue = null;
                if (prizes.isEmpty()) {
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    return;
                }
                stage = PRIZE_REMOVE;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            }
        }
    }

    private void showModalToAddPrize(@NotNull StringSelectInteractionEvent event) {
        TextInput name = TextInput.create(PRIZE_NAME_ID, "Nazwa", TextInputStyle.SHORT)
                .setPlaceholder("Podaj nazwę nagrody")
                .setRequired(true)
                .setRequiredRange(3, 255)
                .build();

        TextInput qty = TextInput.create(PRIZE_QTY_ID, "Ilość", TextInputStyle.SHORT)
                .setPlaceholder("Ilość sztuk")
                .setRequired(true)
                .setRequiredRange(1, 3)
                .build();

        Modal modal = Modal.create(ComponentId.GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD, "Dodawanie nagrody")
                .addComponents(ActionRow.of(name), ActionRow.of(qty))
                .build();
        event.replyModal(modal).queue();
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
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
        } else if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_REMOVE)) {
            buttonRemoveSelected();
        } else if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_REMOVE_ALL)) {
            buttonRemoveAllPrizes();
        } else {
            throw new NoSuchElementException("No such button - " + event.getComponentId());
        }
    }

    private void buttonRemoveSelected() {
        if (selectMenuValue == null) {
            return;
        }
        selectMenuValue = selectMenuValue.substring("prize:" .length());
        prizes.remove(Integer.parseInt(selectMenuValue));
        if (prizes.isEmpty()) {
            stage = PRIZE;
        }
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
    }

    private void buttonRemoveAllPrizes() {
        prizes.clear();
        stage = PRIZE;
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
    }

    private void buttonBack() {
        switch (stage) {
            case TIME_MODE -> message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            case TIME_DURATION, DATE_SELECT -> {
                stage = TIME_MODE;
                selectMenuValue = null;
                buttonBack();
            }
            case TIME_SELECT -> {
                stage = DATE_SELECT;
                selectMenuValue = null;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            }
            case PRIZE_REMOVE -> {
                stage = PRIZE;
                selectMenuValue = null;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
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
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = TIME_MODE;
                } else {
                    if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.DATE_TIME.getValue())) {
                        //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                        selectMenuValue = null;
                        stage = DATE_SELECT;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    } else if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.TIME_DURATION.getValue())) {
                        //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                        selectMenuValue = null;
                        stage = TIME_DURATION;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    }
                }
            }
            case DATE_SELECT -> {
                if (selectMenuValue == null) {
                    stage = DATE_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = DATE_SELECT;
                } else {
                    //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                    LocalDateTime date = SelectMenuOption.getDate(selectMenuValue);
                    selectMenuValue = null;
                    stage = TIME_SELECT;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                }
            }
            case TIME_SELECT -> {
                if (selectMenuValue == null) {
                    stage = TIME_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = TIME_SELECT;
                } else {
                    //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                    selectMenuValue = null;
                    stage = CLAN_MEMBER_EXCLUDE;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                }
            }
            case TIME_DURATION -> {
                if (selectMenuValue == null) {
                    stage = TIME_DURATION_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = TIME_DURATION;
                } else {
                    //TODO zapisanie danej do późniejszego stworzenia rekordu w DB.
                    selectMenuValue = null;
                    stage = CLAN_MEMBER_EXCLUDE;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                }
            }
            case CLAN_MEMBER_EXCLUDE -> {
                if (selectMenuValue == null) {
                    stage = CLAN_MEMBER_EXCLUDE_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = CLAN_MEMBER_EXCLUDE;
                } else {
                    //TODO zapisać zmieną czy wybrano tak czy nie wykluczać
                    selectMenuValue = null;
                    stage = PRIZE;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                }
            }
            case PRIZE -> {
                //TODO zakończyć generator
                // Stworzyć giveaway i przekazać do serwisu który utworzy rekordy w DB i utworzy formatke na kanale
            }
            default -> throw new StageNoSupportedException("Stage - " + stage.name());
        }
    }

    public void saveAnswer(@NotNull ModalInteractionEvent event) {
        boolean isCorrect = false;
        String name = Objects.requireNonNull(event.getValue(PRIZE_NAME_ID)).getAsString();
        String qty = Objects.requireNonNull(event.getValue(PRIZE_QTY_ID)).getAsString();
        int qtyInt = 0;
        try {
            qtyInt = Integer.parseInt(qty);
            isCorrect = true;
        } catch (NumberFormatException e) {
            log.info("Cannot create number from input string");
            stage = PRIZE_QTY_NOT_CORRECT;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            stage = PRIZE;
        } finally {
            event.deferEdit().queue();
        }
        if (isCorrect) {
            Prize prize = new Prize();
            prize.setName(name);
            prize.setNumberOfPrizes(qtyInt);
            prizes.add(prize);
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
        }
    }
}

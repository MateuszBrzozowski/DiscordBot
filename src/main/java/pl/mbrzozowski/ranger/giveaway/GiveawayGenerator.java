package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.giveaway.GiveawayGeneratorStage.*;
import static pl.mbrzozowski.ranger.helpers.ComponentId.*;

@Slf4j
public class GiveawayGenerator {

    private static final String PRIZE_NAME_ID = "prizeNameId";
    private static final String PRIZE_QTY_ID = "prizeQtyId";
    private static final int MAX_NUMBER_OF_PRIZE = 25;
    private final User user;
    private final TextChannel channelToPublish;
    private final GiveawayService giveawayService;
    private final List<Prize> prizes = new ArrayList<>();
    private final GiveawayRequest giveawayRequest = new GiveawayRequest();
    private boolean isPathSelectTimeDuration = false;
    private Message message;
    private String selectMenuValue;
    private GiveawayGeneratorStage stage = TIME_MODE;

    public GiveawayGenerator(User user, TextChannel channel, GiveawayService giveawayService) {
        log.info("{} - Open giveaway generator", user);
        this.user = user;
        this.channelToPublish = channel;
        this.giveawayService = giveawayService;
        start();
    }

    private void start() {
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(getEmbed())
                .setComponents(
                        ActionRow.of(getSelectMenu()),
                        ActionRow.of(getButtons())
                )
                .queue(message -> this.message = message));
        log.info("{} - Created new giveaway generator", user);
    }

    @NotNull
    private MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":tada: Giveaway - Generator :tada:\n");
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
                builder.addField("", "- Nie wybrano daty!", false);
                builder.setColor(Color.RED);
            }
            case TIME_SELECT -> builder.setDescription("Wybierz czas zakończenia");
            case TIME_NOT_SELECTED -> {
                builder.setDescription("Wybierz czas zakończenia");
                builder.addField("", """
                        - Nie wybrano czasu lub niepoprawny czas!
                        - Minimalny czas trwania to 1 minuta.
                        """, false);
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
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
            }
            case PRIZE_QTY_NOT_CORRECT -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("", "- Ilość sztuk musi być liczbą!**\n", false);
                builder.setColor(Color.RED);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
            }
            case MAX_NUMBER_OF_PRIZE_STAGE -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("", "- Osiągnięto maksymalną ilość nagród dla tego giveawaya!", false);
                builder.setColor(Color.RED);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
            }
            case PRIZE_REMOVE -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("Usuwanie nagród", "", false);
                builder.setColor(Color.CYAN);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
            }
            case END -> {
                builder.setColor(Color.GREEN);
                builder.setDescription("Giveaway utworzony");
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
            }
            case CANNOT_END -> {
                builder.setColor(Color.RED);
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
                builder.addField("", """
                        - Niepoprawny czas zakończenia! Wróć i wprowadź nowy.
                        - Minimalny czas trwania to 1 minuta.
                        """, false);
            }
            case UNEXPECTED_ERROR -> {
                builder.setDescription("Przerwano generowanie giveawaya");
                builder.setColor(Color.RED);
                builder.addField("", "- Wystąpił nieoczekiwany błąd.", false);
            }
            case CANCEL -> {
                builder.setColor(Color.DARK_GRAY);
                builder.setDescription("Przerwano generowanie giveawaya");
            }
            default -> throw new IllegalArgumentException("Incorrect stage - " + stage.name());
        }

        return builder.build();
    }

    @NotNull
    static String getPrizesDescription(@NotNull List<Prize> prizes) {
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
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Wybierz sposób")
                        .addOptions(SelectMenuOption.getTimeMode())
                        .build();
            }
            case TIME_DURATION, TIME_DURATION_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Ile ma trwać giveaway?")
                        .addOptions(SelectMenuOption.getDurationTimes())
                        .build();
            }
            case DATE_SELECT, DATE_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Wybierz date zakończenia")
                        .addOptions(SelectMenuOption.getDays())
                        .build();
            }
            case TIME_SELECT, TIME_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Wybierz godzinę zakończenia")
                        .addOptions(SelectMenuOption.getHours())
                        .build();
            }
            case CLAN_MEMBER_EXCLUDE, CLAN_MEMBER_EXCLUDE_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Czy wykluczyć Clan Memberów?")
                        .addOptions(SelectMenuOption.getExclude())
                        .build();
            }
            case PRIZE, PRIZE_QTY_NOT_CORRECT, MAX_NUMBER_OF_PRIZE_STAGE, CANNOT_END -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Wybierz opcję")
                        .addOptions(SelectMenuOption.getPrize())
                        .build();
            }
            case PRIZE_REMOVE -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
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
        log.info("Selected value={}", selectMenuValue);
        if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.DATE_TIME.getValue()) ||
                selectMenuValue.equalsIgnoreCase(SelectMenuOption.TIME_DURATION.getValue()) ||
                selectMenuValue.equalsIgnoreCase(SelectMenuOption.EXCLUDE_YES.getValue()) ||
                selectMenuValue.equalsIgnoreCase(SelectMenuOption.EXCLUDE_NO.getValue())) {
            event.getInteraction().deferEdit().queue();
            buttonNext();
            return;
        }
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
        log.info("Open modal{id={}, title={}}  for {}", GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD, modal.getTitle(), user);
    }

    public void cancel() {
        stage = CANCEL;
        message.editMessageEmbeds(getEmbed()).setComponents().queue();
        log.info("{} - Cancel giveaway generator", user);
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_BACK)) {
            log.info("Stage={} button back", stage);
            buttonBack();
        } else if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_NEXT)) {
            log.info("Stage={} button next", stage);
            buttonNext();
        } else if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_REMOVE)) {
            log.info("Stage={} button remove", stage);
            buttonRemoveSelected();
        } else if (event.getComponentId().equalsIgnoreCase(GIVEAWAY_GENERATOR_BTN_REMOVE_ALL)) {
            log.info("Stage={} button remove all", stage);
            buttonRemoveAllPrizes();
        } else {
            throw new NoSuchElementException("No such button - " + event.getComponentId());
        }
    }

    private void buttonRemoveSelected() {
        if (selectMenuValue == null) {
            return;
        }
        selectMenuValue = selectMenuValue.substring("prize:".length());
        Prize remove = prizes.remove(Integer.parseInt(selectMenuValue));
        log.info("removed " + remove);
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
        log.info("Removed all prizes");
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
            case CLAN_MEMBER_EXCLUDE -> {
                if (isPathSelectTimeDuration) {
                    stage = TIME_DURATION;
                } else {
                    stage = TIME_SELECT;
                }
                selectMenuValue = null;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            }
            case PRIZE -> {
                stage = CLAN_MEMBER_EXCLUDE;
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
                        selectMenuValue = null;
                        stage = DATE_SELECT;
                        isPathSelectTimeDuration = false;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    } else if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.TIME_DURATION.getValue())) {
                        selectMenuValue = null;
                        stage = TIME_DURATION;
                        isPathSelectTimeDuration = true;
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
                    SelectMenuOption date = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setExactDate(date);
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
                    SelectMenuOption time = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setExactTime(time);
                    if (giveawayRequest.isEndTimeAfterNow()) {
                        selectMenuValue = null;
                        stage = CLAN_MEMBER_EXCLUDE;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    } else {
                        stage = TIME_NOT_SELECTED;
                        message.editMessageEmbeds(getEmbed())
                                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                        stage = TIME_SELECT;
                    }
                }
            }
            case TIME_DURATION -> {
                if (selectMenuValue == null) {
                    stage = TIME_DURATION_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = TIME_DURATION;
                } else {
                    SelectMenuOption timeDuration = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setTimeDuration(timeDuration);
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
                    SelectMenuOption excludeClanMember = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setClanMemberExclude(excludeClanMember);
                    selectMenuValue = null;
                    stage = PRIZE;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                }
            }
            case PRIZE -> {
                giveawayRequest.setStartTime();
                if (giveawayRequest.isEndTimeAfterNow()) {
                    stage = END;
                    message.editMessageEmbeds(getEmbed()).setComponents().queue();
                    Giveaway giveaway = giveawayRequest.getGiveaway();
                    giveawayService.publishOnChannel(channelToPublish, giveaway, prizes);
                } else {
                    stage = CANNOT_END;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                    stage = PRIZE;
                }
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

    public boolean isActualActiveGenerator(@NotNull ButtonInteractionEvent event) {
        return message.getId().equalsIgnoreCase(event.getMessage().getId());
    }

    public boolean userHasActiveGenerator(@NotNull User user) {
        return this.user.getId().equalsIgnoreCase(user.getId());
    }

    protected User getUser() {
        return user;
    }
}

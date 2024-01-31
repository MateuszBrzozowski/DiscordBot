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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.exceptions.IllegalStageException;
import pl.mbrzozowski.ranger.exceptions.StageNoSupportedException;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.model.SelectMenuOption;

import java.awt.*;
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.giveaway.GiveawayGeneratorStage.*;
import static pl.mbrzozowski.ranger.helpers.ComponentId.*;

@Slf4j
public class GiveawayGenerator {

    private static final String MODAL_INPUT_1 = "MI_01";
    private static final String MODAL_INPUT_2 = "MI_02";
    private final SelectMenuOption selectMenuOption = new SelectMenuOption();
    private final List<Prize> prizes = new ArrayList<>();
    private final GiveawayService giveawayService;
    private final GiveawayRequest giveawayRequest;
    private final User user;
    private List<SelectOption> selectMenuOptionList = new ArrayList<>();
    private boolean isPathSelectTimeDuration = false;
    private GiveawayGeneratorStage stage = TIME_MODE;
    private String selectMenuValue;
    private Message message;

    public GiveawayGenerator(User user, TextChannel channel, GiveawayService giveawayService) {
        log.info("{} - Open giveaway generator", user);
        this.giveawayRequest = new GiveawayRequest(channel);
        this.giveawayService = giveawayService;
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
        log.info("{} - Created new giveaway generator", user);
    }

    @NotNull
    private MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":tada: Giveaway - Generator :tada:\n");
        builder.setColor(new Color(143, 209, 207));
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
            case RULES -> {
                builder.setDescription("Regulamin");
                addRulesField(builder);
                builder.addField("", "- Dodaj regulamin (opcjonalnie)", false);
            }
            case RULES_NOT_CORRECT -> {
                builder.setColor(Color.RED);
                builder.setDescription("Regulamin");
                builder.addField("", "- Niepoprawny link.\n" +
                        "- Musi zaczynać się od *http://* lub *https://*", false);
            }
            case PING -> builder.setDescription("Czy oznaczyć **@everyone** przy publikacji giveawaya?");
            case PRIZE -> {
                builder.setDescription("Ustaw nagrody");
                addPrizeField(builder);
            }
            case PRIZE_QTY_NOT_CORRECT -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("", "- Ilość sztuk musi być liczbą!**\n", false);
                builder.setColor(Color.RED);
                addPrizeField(builder);
            }
            case MAX_NUMBER_OF_PRIZE_STAGE -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("", "- Osiągnięto maksymalną ilość nagród dla tego giveawaya!", false);
                builder.setColor(Color.RED);
                addPrizeField(builder);
            }
            case PRIZE_REMOVE -> {
                builder.setDescription("Ustaw nagrody");
                builder.addField("Usuwanie nagród", "", false);
                builder.setColor(new Color(108, 76, 150));
                addPrizeField(builder);
            }
            case END -> {
                builder.setColor(new Color(151, 1, 95));
                builder.setDescription("Giveaway utworzony");
                addPrizeField(builder);
                addRulesField(builder);
                addEndTimeField(builder);
            }
            case CANNOT_END -> {
                builder.setColor(Color.RED);
                addPrizeField(builder);
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
                builder.setColor(new Color(46, 1, 73));
                builder.setDescription("Przerwano generowanie giveawaya");
            }
            default -> throw new IllegalArgumentException("Incorrect stage - " + stage.name());
        }

        return builder.build();
    }

    private void addEndTimeField(@NotNull EmbedBuilder builder) {
        builder.addField("Zakończenie:", Converter.LocalDateTimeToTimestampDateTimeLongFormat(giveawayRequest.getEndTime()), false);
    }

    private void addRulesField(EmbedBuilder builder) {
        if (StringUtils.isNotBlank(giveawayRequest.getRulesLink())) {
            builder.addField("", "[Regulamin](" + giveawayRequest.getRulesLink() + ")", false);
        }
    }

    private void addPrizeField(EmbedBuilder builder) {
        if (!prizes.isEmpty()) {
            builder.addField("Nagrody:", getPrizesDescription(prizes), false);
        }
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
                selectMenuOptionList = selectMenuOption.getOptions("Określę dokładną datę i godzinę", "Określę czas trwania");
                return getStringSelectMenu("Wybierz sposób", selectMenuOptionList);
            }
            case TIME_DURATION, TIME_DURATION_NOT_SELECTED -> {
                return getStringSelectMenu("Ile ma trwać giveaway?", SelectMenuOptionTime.getDurationTimes());
            }
            case DATE_SELECT, DATE_NOT_SELECTED -> {
                return getStringSelectMenu("Wybierz date zakończenia", SelectMenuOptionTime.getDays());
            }
            case TIME_SELECT, TIME_NOT_SELECTED -> {
                return getStringSelectMenu("Wybierz godzinę zakończenia", SelectMenuOptionTime.getHours());
            }
            case CLAN_MEMBER_EXCLUDE -> {
                selectMenuOptionList = selectMenuOption.getOptions("Tak", "Nie");
                giveawayRequest.setClanMemberExclude(false);
                return getStringSelectMenu("Czy wykluczyć Clan Mamberów?", selectMenuOptionList, selectMenuOptionList.get(1));
            }
            case RULES, RULES_NOT_CORRECT -> {
                selectMenuOptionList = selectMenuOption.getOptions("Dodaj/Edytuj");
                return getStringSelectMenu("Regulamin", selectMenuOptionList);
            }
            case PING -> {
                selectMenuOptionList = selectMenuOption.getOptions("Tak", "Nie");
                giveawayRequest.setMentionEveryone(true);
                return getStringSelectMenu("Oznaczyć?", selectMenuOptionList, selectMenuOptionList.get(0));
            }
            case PRIZE, PRIZE_QTY_NOT_CORRECT, MAX_NUMBER_OF_PRIZE_STAGE, CANNOT_END -> {
                selectMenuOptionList = selectMenuOption.getOptions("Dodaj nagrodę", "Usuń nagrodę");
                return getStringSelectMenu("Wybierz opcję", selectMenuOptionList);
            }
            case PRIZE_REMOVE -> {
                return getStringSelectMenu("Wybierz nagrodę do usunięcia", getPrizesAsSelectMenuOptions());
            }
            default -> throw new IllegalStageException(stage);
        }
    }

    /**
     * Create and return {@link  StringSelectMenu} with default option
     *
     * @param placeholder         The placeholder or null
     * @param selectOptions       The {@link SelectOption} to add
     * @param defaultSelectOption The default {@link SelectOption}
     * @return {@link  StringSelectMenu} with default option
     */
    @NotNull
    private StringSelectMenu getStringSelectMenu(String placeholder,
                                                 Collection<? extends SelectOption> selectOptions,
                                                 @NotNull SelectOption defaultSelectOption) {
        return StringSelectMenu
                .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                .setPlaceholder(placeholder)
                .setDefaultValues(defaultSelectOption.getValue())
                .setPlaceholder(defaultSelectOption.getLabel())
                .addOptions(selectOptions)
                .build();
    }

    /**
     * Create and return {@link  StringSelectMenu}
     *
     * @param placeholder   The placeholder or null
     * @param selectOptions The {@link SelectOption} to add
     * @return {@link  StringSelectMenu}
     */
    @NotNull
    private StringSelectMenu getStringSelectMenu(String placeholder, Collection<? extends SelectOption> selectOptions) {
        return StringSelectMenu
                .create(ComponentId.GIVEAWAY_GENERATOR_SELECT_MENU)
                .setPlaceholder(placeholder)
                .addOptions(selectOptions)
                .build();
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
        switch (stage) {
            case PRIZE_REMOVE -> {
                buttons.add(Button.primary(GIVEAWAY_GENERATOR_BTN_REMOVE, "Usuń zaznaczony"));
                buttons.add(Button.primary(GIVEAWAY_GENERATOR_BTN_REMOVE_ALL, "Usuń wszystkie"));
                buttons.add(Button.primary(GIVEAWAY_GENERATOR_BTN_BACK, "Powrót"));
            }
            case TIME_MODE, TIME_MODE_NOT_SELECTED -> {
                buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz").asDisabled());
                buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Dalej"));
            }
            case PRIZE -> {
                if (prizes.isEmpty()) {
                    buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz"));
                    buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Zakończ").asDisabled());
                } else {
                    buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz"));
                    buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_NEXT, "Zakończ"));
                }
            }
            default -> {
                buttons.add(Button.success(GIVEAWAY_GENERATOR_BTN_BACK, "Wstecz"));
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
        if (selectMenuOptionList.size() > 0 && selectMenuValue.equals(selectMenuOptionList.get(0).getValue())) {
            switch (stage) {
                case TIME_MODE, TIME_MODE_NOT_SELECTED, CLAN_MEMBER_EXCLUDE -> {
                    event.deferEdit().queue();
                    buttonNext();
                }
                case RULES, RULES_NOT_CORRECT -> openModalRulesLink(event);
                case PING -> {
                    event.deferEdit().queue();
                    giveawayRequest.setMentionEveryone(true);
                }
                case PRIZE, MAX_NUMBER_OF_PRIZE_STAGE -> {
                    if (prizes.size() < SelectMenu.OPTIONS_MAX_AMOUNT) {
                        openModalToAddPrize(event);
                    } else {
                        event.deferEdit().queue();
                        setMessageEmbed(MAX_NUMBER_OF_PRIZE_STAGE, 1);
                        stage = PRIZE;
                    }
                }
                default -> throw new IllegalStageException(stage);
            }
        } else if (selectMenuOptionList.size() > 1 && selectMenuValue.equals(selectMenuOptionList.get(1).getValue())) {
            event.deferEdit().queue();
            switch (stage) {
                case TIME_MODE, TIME_MODE_NOT_SELECTED, CLAN_MEMBER_EXCLUDE -> buttonNext();
                case PING -> giveawayRequest.setMentionEveryone(false);
                case PRIZE, PRIZE_REMOVE, MAX_NUMBER_OF_PRIZE_STAGE -> {
                    selectMenuValue = null;
                    if (prizes.isEmpty()) {
                        setMessageEmbed(stage, 1);
                        return;
                    }
                    setMessageEmbed(PRIZE_REMOVE, 1);
                }
                default -> throw new IllegalStageException(stage);
            }
        } else {
            event.deferEdit().queue();
        }
    }

    private void openModalRulesLink(@NotNull StringSelectInteractionEvent event) {
        TextInput link = TextInput.create(MODAL_INPUT_1, "Link", TextInputStyle.SHORT)
                .setPlaceholder("Link (http://example.com)")
                .setRequired(false)
                .setMaxLength(1024)
                .build();
        if (StringUtils.isNotBlank(giveawayRequest.getRulesLink())) {
            link = TextInput.create(MODAL_INPUT_1, "Link", TextInputStyle.SHORT)
                    .setPlaceholder("Link (http://example.com)")
                    .setValue(giveawayRequest.getRulesLink())
                    .setRequired(false)
                    .setMaxLength(1024)
                    .build();
        }
        Modal modal = Modal.create(GIVEAWAY_GENERATOR_MODAL_RULES_LINK, "Link do regulaminu")
                .addComponents(ActionRow.of(link))
                .build();
        event.replyModal(modal).queue();
        log.info("Open modal{id={}, title={}}  for {}", GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD, modal.getTitle(), user);
    }

    private void openModalToAddPrize(@NotNull StringSelectInteractionEvent event) {
        TextInput name = TextInput.create(MODAL_INPUT_1, "Nazwa", TextInputStyle.SHORT)
                .setPlaceholder("Podaj nazwę nagrody")
                .setRequired(true)
                .setRequiredRange(3, 255)
                .build();

        TextInput qty = TextInput.create(MODAL_INPUT_2, "Ilość", TextInputStyle.SHORT)
                .setPlaceholder("Ilość sztuk")
                .setRequired(true)
                .setRequiredRange(1, 3)
                .build();

        Modal modal = Modal.create(ComponentId.GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD, "Dodawanie nagrody")
                .addComponents(ActionRow.of(name), ActionRow.of(qty))
                .build();
        event.replyModal(modal).queue();
        setMessageEmbed(stage, 1);
        log.info("Open modal{id={}, title={}}  for {}", GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD, modal.getTitle(), user);
    }

    public void cancel() {
        setMessageEmbed(CANCEL, 0);
        log.info("{} - Cancel giveaway generator", user);
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        log.info("Stage={} buttonId={}", stage, event.getComponentId());
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
        selectMenuValue = selectMenuValue.substring("prize:".length());
        Prize remove = prizes.remove(Integer.parseInt(selectMenuValue));
        log.info("removed " + remove);
        if (prizes.isEmpty()) {
            stage = PRIZE;
        }
        setMessageEmbed(stage, 1);
    }

    private void buttonRemoveAllPrizes() {
        prizes.clear();
        setMessageEmbed(PRIZE, 1);
        log.info("Removed all prizes");
    }

    private void buttonBack() {
        switch (stage) {
            case TIME_MODE -> setMessageEmbed(TIME_MODE, 1);
            case TIME_DURATION, TIME_DURATION_NOT_SELECTED, DATE_SELECT, DATE_NOT_SELECTED -> {
                stage = TIME_MODE;
                buttonBack();
            }
            case TIME_SELECT, TIME_NOT_SELECTED -> setMessageEmbed(DATE_SELECT, 1);
            case CLAN_MEMBER_EXCLUDE -> {
                if (isPathSelectTimeDuration) {
                    setMessageEmbed(TIME_DURATION, 1);
                } else {
                    setMessageEmbed(TIME_SELECT, 1);
                }
            }
            case RULES, RULES_NOT_CORRECT -> setMessageEmbed(CLAN_MEMBER_EXCLUDE, 1);
            case PING -> {
                giveawayRequest.setMentionEveryone(false);
                setMessageEmbed(RULES, 1);
            }
            case PRIZE, CANNOT_END -> {
                giveawayRequest.setMentionEveryone(false);
                setMessageEmbed(PING, 1);
            }
            case PRIZE_REMOVE -> setMessageEmbed(PRIZE, 1);
            default -> throw new StageNoSupportedException("Stage - " + stage.name());
        }
        selectMenuValue = null;
    }

    private void buttonNext() {
        switch (stage) {
            case TIME_MODE, TIME_MODE_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(TIME_MODE_NOT_SELECTED, 1);
                } else {
                    if (selectMenuValue.equals(selectMenuOptionList.get(0).getValue())) {
                        isPathSelectTimeDuration = false;
                        setMessageEmbed(DATE_SELECT, 1);
                    } else if (selectMenuValue.equals(selectMenuOptionList.get(1).getValue())) {
                        isPathSelectTimeDuration = true;
                        setMessageEmbed(TIME_DURATION, 1);
                    }
                }
            }
            case DATE_SELECT, DATE_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(DATE_NOT_SELECTED, 1);
                } else {
                    SelectMenuOptionTime date = SelectMenuOptionTime.getByValue(selectMenuValue);
                    giveawayRequest.setExactDate(date);
                    setMessageEmbed(TIME_SELECT, 1);
                }
            }
            case TIME_SELECT, TIME_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(TIME_NOT_SELECTED, 1);
                } else {
                    SelectMenuOptionTime time = SelectMenuOptionTime.getByValue(selectMenuValue);
                    giveawayRequest.setExactTime(time);
                    if (giveawayRequest.isEndTimeAfterNow()) {
                        setMessageEmbed(CLAN_MEMBER_EXCLUDE, 1);
                    } else {
                        setMessageEmbed(TIME_NOT_SELECTED, 1);
                    }
                }
            }
            case TIME_DURATION, TIME_DURATION_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(TIME_DURATION_NOT_SELECTED, 1);
                } else {
                    SelectMenuOptionTime timeDuration = SelectMenuOptionTime.getByValue(selectMenuValue);
                    giveawayRequest.setTimeDuration(timeDuration);
                    setMessageEmbed(CLAN_MEMBER_EXCLUDE, 1);
                }
            }
            case CLAN_MEMBER_EXCLUDE -> {
                giveawayRequest.setClanMemberExclude(selectMenuOptionList, selectMenuValue);
                setMessageEmbed(RULES, 1);
            }
            case RULES, RULES_NOT_CORRECT -> setMessageEmbed(PING, 1);
            case PING -> setMessageEmbed(PRIZE, 1);
            case PRIZE, CANNOT_END -> {
                giveawayRequest.setStartTime();
                if (giveawayRequest.isEndTimeAfterNow()) {
                    setMessageEmbed(END, 0);
                    log.info("{}", giveawayRequest);
                    giveawayService.removeGenerator();
                    giveawayService.publishOnChannel(giveawayRequest.getChannelToPublish(), giveawayRequest, prizes);
                } else {
                    setMessageEmbed(CANNOT_END, 1);
                }
            }
            default -> throw new StageNoSupportedException("Stage - " + stage.name());
        }
        selectMenuValue = null;
    }

    public void submit(@NotNull ModalInteractionEvent event) {
        event.deferEdit().queue();
        String modalId = event.getModalId();
        switch (modalId) {
            case GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD -> submitPrize(event);
            case GIVEAWAY_GENERATOR_MODAL_RULES_LINK -> submitRulesLink(event);
            default -> throw new IllegalStateException("ModalId=" + modalId);
        }
    }

    private void submitRulesLink(@NotNull ModalInteractionEvent event) {
        giveawayRequest.setRulesLink(Objects.requireNonNull(event.getValue(MODAL_INPUT_1)).getAsString());
        if (giveawayRequest.getRulesLink().startsWith("http://") ||
                giveawayRequest.getRulesLink().startsWith("https://") ||
                giveawayRequest.getRulesLink().length() == 0) {
            stage = RULES;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
        } else {
            giveawayRequest.setRulesLink("");
            stage = RULES_NOT_CORRECT;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
        }

    }

    private void submitPrize(@NotNull ModalInteractionEvent event) {
        String name = Objects.requireNonNull(event.getValue(MODAL_INPUT_1)).getAsString();
        String qty = Objects.requireNonNull(event.getValue(MODAL_INPUT_2)).getAsString();
        boolean isCorrect = false;
        int qtyInt = 0;
        try {
            qtyInt = Integer.parseInt(qty);
            isCorrect = true;
        } catch (NumberFormatException e) {
            log.info("Cannot create number from input string");
            setMessageEmbed(PRIZE_QTY_NOT_CORRECT, 1);
            stage = PRIZE;
        }
        if (isCorrect) {
            Prize prize = createPrize(name, qtyInt);
            prizes.add(prize);
            setMessageEmbed(stage, 1);
        }
    }

    @NotNull
    private Prize createPrize(String name, int quantity) {
        Prize prize = new Prize();
        prize.setName(name);
        prize.setNumberOfPrizes(quantity);
        return prize;
    }

    /**
     * Edit and updated message with selected components' via mode
     *
     * @param stage of giveaway generator
     * @param mode  1 - set select menu + buttons;
     *              2 - set only buttons;
     *              3 - set only select menu;
     *              0 - only embed without select menu and buttons
     * @throws IllegalArgumentException if int mode not supported
     */
    private void setMessageEmbed(@NotNull GiveawayGeneratorStage stage, int mode) {
        this.stage = stage;
        switch (mode) {
            case 1 -> message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            case 2 -> message.editMessageEmbeds(getEmbed()).setComponents(ActionRow.of(getButtons())).queue();
            case 3 -> message.editMessageEmbeds(getEmbed()).setComponents(ActionRow.of(getSelectMenu())).queue();
            case 0 -> message.editMessageEmbeds(getEmbed()).setComponents().queue();
            default -> throw new IllegalArgumentException("Mode=" + mode + " not supported");
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

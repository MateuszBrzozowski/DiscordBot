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

import java.awt.*;
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.giveaway.GiveawayGeneratorStage.*;
import static pl.mbrzozowski.ranger.helpers.ComponentId.*;

@Slf4j
public class GiveawayGenerator {

    private static final String MODAL_INPUT_1 = "MI_01";
    private static final String MODAL_INPUT_2 = "MI_02";
    private static final String SELECT_MENU_OPTION_1 = "SMO_01";
    private static final String SELECT_MENU_OPTION_2 = "SMO_02";
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
//    private String rulesLink = "";
//    private boolean mentionEveryone = false;

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
            case RULES -> {
                builder.setDescription("Regulamin");
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
                if (StringUtils.isNotBlank(giveawayRequest.getRulesLink())) {
                    builder.addField("", "[Regulamin](" + giveawayRequest.getRulesLink() + ")", false);
                }
                builder.addField("", "- Dodaj regulamin (opcjonalnie)", false);
            }
            case RULES_NOT_CORRECT -> {
                builder.setColor(Color.RED);
                builder.setDescription("Regulamin");
                if (!prizes.isEmpty()) {
                    builder.addField("Nagrody:", getPrizesDescription(prizes), false);
                }
                builder.addField("", "- Niepoprawny link.\n" +
                        "- Musi zaczynać się od *http://* lub *https://*", false);
            }
            case PING -> builder.setDescription("Czy oznaczyć **@everyone** przy publikacji giveawaya?");
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
            case RULES, RULES_NOT_CORRECT -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Regulamin")
                        .addOption("Dodaj/Edytuj", SELECT_MENU_OPTION_1)
                        .build();
            }
            case PING -> {
                return StringSelectMenu
                        .create(GIVEAWAY_GENERATOR_SELECT_MENU)
                        .setPlaceholder("Nie")
                        .setDefaultValues(SELECT_MENU_OPTION_2)
                        .addOption("Tak", SELECT_MENU_OPTION_1)
                        .addOption("Nie", SELECT_MENU_OPTION_2)
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
        if (selectMenuValue.equals(SelectMenuOption.DATE_TIME.getValue()) ||
                selectMenuValue.equals(SelectMenuOption.TIME_DURATION.getValue()) ||
                selectMenuValue.equals(SelectMenuOption.EXCLUDE_YES.getValue()) ||
                selectMenuValue.equals(SelectMenuOption.EXCLUDE_NO.getValue())) {
            event.deferEdit().queue();
            buttonNext();
        } else if (selectMenuValue.equals(SELECT_MENU_OPTION_1)) {
            switch (stage) {
                case RULES, RULES_NOT_CORRECT -> openModalRulesLink(event);
                case PING -> {
                    event.getInteraction().deferEdit().queue();
                    giveawayRequest.setMentionEveryone(true);
                }
                default -> throw new IllegalStageException(stage);
            }

        } else if (selectMenuValue.equals(SELECT_MENU_OPTION_2)) {
            event.getInteraction().deferEdit().queue();
            giveawayRequest.setMentionEveryone(false);
        } else if (selectMenuValue.equals(SelectMenuOption.ADD_PRIZE.getValue())) {
            if (prizes.size() < MAX_NUMBER_OF_PRIZE) {
                openModalToAddPrize(event);
            } else {
                event.getInteraction().deferEdit().queue();
                setMessageEmbed(MAX_NUMBER_OF_PRIZE_STAGE, 1);
                stage = PRIZE;
            }
        } else if (selectMenuValue.equals(SelectMenuOption.REMOVE_PRIZE.getValue())) {
            event.deferEdit().queue();
            selectMenuValue = null;
            if (prizes.isEmpty()) {
                setMessageEmbed(stage, 1);
                return;
            }
            setMessageEmbed(PRIZE_REMOVE, 1);
        } else {
            event.getInteraction().deferEdit().queue();
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
            case CLAN_MEMBER_EXCLUDE, CLAN_MEMBER_EXCLUDE_NOT_SELECTED -> {
                if (isPathSelectTimeDuration) {
                    setMessageEmbed(TIME_DURATION, 1);
                } else {
                    setMessageEmbed(TIME_SELECT, 1);
                }
            }
            case RULES, RULES_NOT_CORRECT -> setMessageEmbed(CLAN_MEMBER_EXCLUDE, 1);
            case PING -> setMessageEmbed(RULES, 1);
            case PRIZE, CANNOT_END -> setMessageEmbed(PING, 1);
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
                    if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.DATE_TIME.getValue())) {
                        selectMenuValue = null;
                        isPathSelectTimeDuration = false;
                        setMessageEmbed(DATE_SELECT, 1);
                    } else if (selectMenuValue.equalsIgnoreCase(SelectMenuOption.TIME_DURATION.getValue())) {
                        selectMenuValue = null;
                        isPathSelectTimeDuration = true;
                        setMessageEmbed(TIME_DURATION, 1);
                    }
                }
            }
            case DATE_SELECT, DATE_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(DATE_NOT_SELECTED, 1);
                } else {
                    SelectMenuOption date = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setExactDate(date);
                    selectMenuValue = null;
                    setMessageEmbed(TIME_SELECT, 1);
                }
            }
            case TIME_SELECT, TIME_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(TIME_NOT_SELECTED, 1);
                } else {
                    SelectMenuOption time = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setExactTime(time);
                    if (giveawayRequest.isEndTimeAfterNow()) {
                        selectMenuValue = null;
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
                    SelectMenuOption timeDuration = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setTimeDuration(timeDuration);
                    selectMenuValue = null;
                    setMessageEmbed(CLAN_MEMBER_EXCLUDE, 1);
                }
            }
            case CLAN_MEMBER_EXCLUDE, CLAN_MEMBER_EXCLUDE_NOT_SELECTED -> {
                if (selectMenuValue == null) {
                    setMessageEmbed(CLAN_MEMBER_EXCLUDE_NOT_SELECTED, 1);
                } else {
                    SelectMenuOption excludeClanMember = SelectMenuOption.getByValue(selectMenuValue);
                    giveawayRequest.setClanMemberExclude(excludeClanMember);
                    selectMenuValue = null;
                    setMessageEmbed(RULES, 1);
                }
            }
            case RULES, RULES_NOT_CORRECT -> setMessageEmbed(PING, 1);
            case PING -> setMessageEmbed(PRIZE, 1);
            case PRIZE, CANNOT_END -> {
                giveawayRequest.setStartTime();
                if (giveawayRequest.isEndTimeAfterNow()) {
                    setMessageEmbed(END, 0);
                    Giveaway giveaway = giveawayRequest.getGiveaway();
                    giveawayService.publishOnChannel(channelToPublish, giveawayRequest, prizes);
                } else {
                    setMessageEmbed(CANNOT_END, 1);
                }
            }
            default -> throw new StageNoSupportedException("Stage - " + stage.name());
        }
    }

    public void submit(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        switch (modalId) {
            case GIVEAWAY_GENERATOR_PRIZE_MODAL_ADD -> submitPrize(event);
            case GIVEAWAY_GENERATOR_MODAL_RULES_LINK -> submitRulesLink(event);
            default -> throw new IllegalStateException("ModalId=" + modalId);
        }
    }

    private void submitRulesLink(@NotNull ModalInteractionEvent event) {
        event.deferEdit().queue();
        giveawayRequest.setRulesLink(Objects.requireNonNull(event.getValue(MODAL_INPUT_1)).getAsString());
        if (giveawayRequest.getRulesLink().startsWith("http://") || giveawayRequest.getRulesLink().startsWith("https://")) {
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
        boolean isCorrect = false;
        String name = Objects.requireNonNull(event.getValue(MODAL_INPUT_1)).getAsString();
        String qty = Objects.requireNonNull(event.getValue(MODAL_INPUT_2)).getAsString();
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

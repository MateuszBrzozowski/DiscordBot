package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.exceptions.IllegalStageException;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.helpers.Validator;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.guild.ComponentId.*;

@Slf4j
public class EventsSettings {

    private static final String TEXT_INPUT_ID_1 = "textInputId1";
    private static final String TEXT_INPUT_ID_2 = "textInputId2";
    private final EventService eventService;
    private final String userId;
    private final EventsSettingsService eventsSettingsService;
    private Message message;
    private Event event;
    private String description = "";
    private List<Event> activeEvents = new ArrayList<>();
    private EventSettingsStatus stageOfSettings = EventSettingsStatus.START;
    private boolean isChangedDateTime = false;
    private boolean isChangedName = false;
    private boolean isChangedDescription = false;
    private boolean isEndingEvent = false;
    private boolean sendNotifi = false;
    private String selectMenuValue;
    private static final String VALUE_SELECT_MENU_OPTION_2 = "valueSelectMenuOption1";
    private static final String VALUE_SELECT_MENU_OPTION_1 = "valueSelectMenuOption2";
    private static final String VALUE_SELECT_MENU_OPTION_3 = "valueSelectMenuOption3";


    public EventsSettings(EventService eventService,
                          @NotNull MessageReceivedEvent privateEvent,
                          EventsSettingsService eventsSettingsService) {
        this.eventService = eventService;
        this.userId = privateEvent.getAuthor().getId();
        this.eventsSettingsService = eventsSettingsService;
        start();
    }

    public EventsSettings(@NotNull EventService eventService,
                          @NotNull ButtonInteractionEvent buttonEvent,
                          EventsSettingsService eventsSettingsService) {
        buttonEvent.deferEdit().queue();
        this.eventService = eventService;
        this.userId = buttonEvent.getUser().getId();
        this.eventsSettingsService = eventsSettingsService;
        this.message = buttonEvent.getMessage();
        activeEvents = eventService.findByIsActive();
        if (activeEvents.isEmpty()) {
            setStageOfSettings(EventSettingsStatus.NO_EVENTS, false, false);
        } else {
            setStageOfSettings(EventSettingsStatus.START, true, true);
        }
    }

    public EventsSettings(EventService eventService,
                          @NotNull SlashCommandInteractionEvent event,
                          EventsSettingsService eventsSettingsService) {
        this.eventService = eventService;
        this.userId = event.getUser().getId();
        this.eventsSettingsService = eventsSettingsService;
        start();
    }

    private void start() {
        activeEvents = eventService.findByIsActive();
        User user = getUserById(userId);
        if (activeEvents.isEmpty()) {
            stageOfSettings = EventSettingsStatus.NO_EVENTS;
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(getEmbed()).queue());
            eventsSettingsService.removeSettingsPanel(userId);
        } else {
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue(message -> this.message = message));
        }
    }

    @NotNull
    private StringSelectMenu getSelectMenu() {
        switch (stageOfSettings) {
            case START, EVENT_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(EVENT_SETTINGS_SELECT_MENU)
                        .setPlaceholder("Wybierz event")
                        .addOptions(getAllEvents())
                        .build();
            }
            case WHAT_TO_DO, DATE_TIME_TITLE_DESC_CHANGED, DATE_TIME_NOT_CORRECT -> {
                return StringSelectMenu
                        .create(EVENT_SETTINGS_SELECT_MENU)
                        .setPlaceholder("Co chcesz zrobić?")
                        .addOption("Zmień nazwę lub opis", VALUE_SELECT_MENU_OPTION_1)
                        .addOption("Zmień date lub czas", VALUE_SELECT_MENU_OPTION_2)
                        .addOption("Odwołaj", VALUE_SELECT_MENU_OPTION_3)
                        .build();
            }
            case EVENT_CANCEL, SEND_NOTIFI, EVENT_CANCEL_NOT_SELECT, SEND_NOTIFI_NOT_SELECT -> {
                return StringSelectMenu
                        .create(EVENT_SETTINGS_SELECT_MENU)
                        .setPlaceholder("Wybierz")
                        .addOption("Tak", VALUE_SELECT_MENU_OPTION_1)
                        .addOption("Nie", VALUE_SELECT_MENU_OPTION_2)
                        .build();
            }
            default -> throw new IllegalStageException(stageOfSettings);
        }
    }

    @NotNull
    private Collection<? extends SelectOption> getAllEvents() {
        List<SelectOption> selectOptions = new ArrayList<>();
        for (Event activeEvent : activeEvents) {
            selectOptions.add(
                    SelectOption.of(activeEvent.getName() + " - " + activeEvent.getDate().getDayOfMonth() + "." +
                                    String.format("%02d", activeEvent.getDate().getMonthValue()) + "." + activeEvent.getDate().getYear(),
                            activeEvent.getMsgId()));
        }
        return selectOptions;
    }

    @NotNull
    private Collection<? extends ItemComponent> getButtons() {
        List<Button> buttons = new ArrayList<>();
        switch (stageOfSettings) {
            case START, EVENT_NOT_SELECTED -> {
                buttons.add(Button.primary(EVENT_SETTINGS_BTN_BACK, "⮜ Wstecz").asDisabled());
                buttons.add(Button.primary(EVENT_SETTINGS_BTN_NEXT, "Dalej ⮞"));
            }
            case WHAT_TO_DO, DATE_TIME_TITLE_DESC_CHANGED, DATE_TIME_NOT_CORRECT, EVENT_CANCEL, EVENT_CANCEL_NOT_SELECT,
                    DATE_TIME_TITLE_DESC_NOT_CHANGED -> {
                buttons.add(Button.primary(EVENT_SETTINGS_BTN_BACK, "⮜ Wstecz"));
                buttons.add(Button.primary(EVENT_SETTINGS_BTN_NEXT, "Dalej ⮞"));
            }
            case SEND_NOTIFI, SEND_NOTIFI_NOT_SELECT -> {
                buttons.add(Button.primary(EVENT_SETTINGS_BTN_BACK, "⮜ Wstecz"));
                buttons.add(Button.success(EVENT_SETTINGS_BTN_NEXT, "Zapisz ⮞"));
            }
            case END, END_NO_CHANGES -> buttons.add(Button.primary(EVENT_SETTINGS_GO_TO_START, "⮜ Do wyboru eventu"));
            default -> throw new IllegalStageException(stageOfSettings);
        }
        buttons.add(Button.danger(EVENT_SETTINGS_BTN_CANCEL, "Wyjdź ❌"));
        return buttons;
    }

    @NotNull
    private MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setDescription("## :calendar_spiral: Wydarzenia - Edytor :calendar_spiral: ");
        switch (stageOfSettings) {
            case START -> builder.addField("", "- Wybierz event który chcesz edytować", false);
            case NO_EVENTS -> {
                builder.setColor(Color.DARK_GRAY);
                builder.addField("", "Brak aktywnych eventów", false);
            }
            case EVENT_NOT_SELECTED -> {
                builder.setColor(Color.RED);
                builder.addField("Błąd", "- Nie wybrano eventu", false);
            }
            case WHAT_TO_DO -> {
                TextChannel textChannel = DiscordBot.getJda().getTextChannelById(event.getChannelId());
                if (textChannel == null) {
                    throw new IllegalArgumentException("TextChannel not exist");
                }
                Message retrievedMessage = textChannel.retrieveMessageById(event.getMsgId()).complete();
                description = retrievedMessage.getEmbeds().get(0).getDescription();
                if (description == null) {
                    description = "";
                }
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
            }
            case DATE_TIME_NOT_CORRECT -> {
                builder.setColor(Color.RED);
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
                builder.addField("===========", "- Data lub czas niepoprawny", false);
            }
            case DATE_TIME_TITLE_DESC_CHANGED -> {
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
            }
            case EVENT_CANCEL -> {
                builder.setColor(Color.ORANGE);
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
                builder.addField("", "==========", false);
                builder.addField("", "Czy na pewno chcesz odwołać event?", false);
            }
            case EVENT_CANCEL_NOT_SELECT -> {
                builder.setColor(Color.RED);
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
                builder.addField("", "==========", false);
                builder.addField("", "Czy na pewno chcesz odwołać event?", false);
            }
            case DATE_TIME_TITLE_DESC_NOT_CHANGED -> {
                builder.setColor(Color.ORANGE);
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
                builder.addField("", "==========", false);
                builder.addField("", "- Nie wprowadzono żadnych zmian.", false);
            }
            case SEND_NOTIFI -> {
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
                builder.addField("", "==========", false);
                builder.addField("", "- Czy chcesz powiadomić wszystkich zapisanych?", false);
            }
            case SEND_NOTIFI_NOT_SELECT -> {
                builder.setColor(Color.RED);
                builder.addField(event.getName(), description, false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                        true);
                builder.addField("", "==========", false);
                builder.addField("", "- Czy chcesz powiadomić wszystkich zapisanych?", false);
            }
            case END_NO_CHANGES -> {
                builder.setColor(Color.DARK_GRAY);
                builder.addField(event.getName(), "", false);
                builder.addField("", "Nie wprowadzono żadnych zmian", false);
            }
            case END -> {
                builder.setColor(Color.GREEN);
                builder.addField(event.getName(), "", false);
                builder.addField("", "Ustawienia zapisane.", false);
            }
            case CANCEL -> {
                builder.setColor(Color.DARK_GRAY);
                builder.addField("", "Edytor zamknięty", false);
            }
            default -> throw new IllegalStageException(stageOfSettings);
        }
        return builder.build();
    }

    public void selectAnswer(@NotNull StringSelectInteractionEvent event) {
        if (!event.getMessage().equals(message)) {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
        }
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        selectMenuValue = selectedOptions.get(0).getValue();
        if (isSelectActiveEvent()) {
            event.getInteraction().deferEdit().queue();
            buttonNext();
        } else {
            whatToDoOption(event);
        }
    }

    private void whatToDoOption(StringSelectInteractionEvent event) {
        switch (stageOfSettings) {
            case WHAT_TO_DO, DATE_TIME_TITLE_DESC_CHANGED, DATE_TIME_NOT_CORRECT -> {
                switch (selectMenuValue) {
                    case VALUE_SELECT_MENU_OPTION_1 -> openModalTitleDesc(event);
                    case VALUE_SELECT_MENU_OPTION_2 -> openModalDateTimme(event);
                    case VALUE_SELECT_MENU_OPTION_3 -> event.getInteraction().deferEdit().queue();
                }
            }
            default -> event.getInteraction().deferEdit().queue();
        }
    }

    private void openModalTitleDesc(StringSelectInteractionEvent event) {
        TextInput title = TextInput.create(TEXT_INPUT_ID_1, "Nazwa", TextInputStyle.SHORT)
                .setPlaceholder("Podaj nazwę eventu")
                .setRequiredRange(1, 256)
                .setRequired(true)
                .build();

        if (StringUtils.isNotBlank(this.event.getName())) {
            title = TextInput.create(TEXT_INPUT_ID_1, "Nazwa", TextInputStyle.SHORT)
                    .setPlaceholder("Podaj nazwę eventu")
                    .setValue(this.event.getName())
                    .setRequiredRange(1, 256)
                    .setRequired(true)
                    .build();
        }

        TextInput desc = TextInput.create(TEXT_INPUT_ID_2, "Opis", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Podaj opis eventu (opcjonalnie)")
                .setRequired(false)
                .setMaxLength(4000)
                .build();

        if (StringUtils.isNotBlank(description)) {
            desc = TextInput.create(TEXT_INPUT_ID_2, "Opis", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Podaj opis eventu (opcjonalnie)")
                    .setValue(description)
                    .setRequired(false)
                    .setMaxLength(4000)
                    .build();
        }

        Modal modal = Modal.create(ComponentId.EVENT_SETTINGS_MODAL_TITLE, "Nazwa i opis")
                .addComponents(ActionRow.of(title), ActionRow.of(desc))
                .build();
        event.replyModal(modal).queue();
        log.info("Opened modal to set title");
    }

    private void openModalDateTimme(StringSelectInteractionEvent event) {
        TextInput date = TextInput.create(TEXT_INPUT_ID_1, "Data", TextInputStyle.SHORT)
                .setPlaceholder("DD.MM.YYYY")
                .setRequiredRange(9, 10)
                .setRequired(true)
                .build();


        TextInput time = TextInput.create(TEXT_INPUT_ID_2, "Czas", TextInputStyle.SHORT)
                .setPlaceholder("HH:mm")
                .setRequiredRange(4, 5)
                .setRequired(true)
                .build();

        if (this.event.getDate() != null) {
            date = TextInput.create(TEXT_INPUT_ID_1, "Data", TextInputStyle.SHORT)
                    .setPlaceholder("DD.MM.YYYY")
                    .setValue(
                            this.event.getDate().getDayOfMonth() + "." +
                                    String.format("%02d", this.event.getDate().getMonthValue()) + "." +
                                    this.event.getDate().getYear())
                    .setRequiredRange(9, 10)
                    .setRequired(true)
                    .build();

            time = TextInput.create(TEXT_INPUT_ID_2, "Czas", TextInputStyle.SHORT)
                    .setPlaceholder("HH:mm")
                    .setValue(this.event.getDate().getHour() + ":" + String.format("%02d", this.event.getDate().getMinute()))
                    .setRequiredRange(4, 5)
                    .setRequired(true)
                    .build();
        }

        Modal modal = Modal.create(EVENT_SETTINGS_MODAL_DATE_TIME, "Data i czas")
                .addComponents(ActionRow.of(date), ActionRow.of(time))
                .build();
        event.replyModal(modal).queue();
        log.info("Opened modal to set time");
    }

    private boolean isSelectActiveEvent() {
        for (Event activeEvent : activeEvents) {
            if (activeEvent.getMsgId().equals(selectMenuValue)) {
                this.event = activeEvent;
                return true;
            }
        }
        return false;
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        if (!event.getMessage().equals(message)) {
            event.getMessage().delete().queue();
            return;
        }
        String componentId = event.getComponentId();
        log.info("Stage={}, buttonId={}", stageOfSettings, componentId);
        switch (componentId) {
            case EVENT_SETTINGS_BTN_BACK, EVENT_SETTINGS_GO_TO_START -> buttonBack();
            case EVENT_SETTINGS_BTN_NEXT -> buttonNext();
            case EVENT_SETTINGS_BTN_CANCEL -> buttonCancel();
            default -> throw new NoSuchElementException("No such button - " + event.getComponentId());
        }
    }

    private void buttonNext() {
        switch (stageOfSettings) {
            case START, EVENT_NOT_SELECTED -> {
                if (isSelectActiveEvent()) {
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                } else {
                    stageOfSettings = EventSettingsStatus.EVENT_NOT_SELECTED;
                    message.editMessageEmbeds(getEmbed())
                            .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
                }
            }
            case WHAT_TO_DO, DATE_TIME_TITLE_DESC_CHANGED -> {
                if (selectMenuValue == null) {
                    selectMenuValue = "";
                }
                if (selectMenuValue.equals(VALUE_SELECT_MENU_OPTION_3)) {
                    setStageOfSettings(EventSettingsStatus.EVENT_CANCEL, true, true);
                } else {
                    if (isChangedDateTime || isChangedName || isChangedDescription) {
                        setStageOfSettings(EventSettingsStatus.SEND_NOTIFI, true, true);
                    } else {
                        setStageOfSettings(EventSettingsStatus.DATE_TIME_TITLE_DESC_NOT_CHANGED, false, true);
                    }
                }
            }
            case DATE_TIME_NOT_CORRECT -> {

            }
            case EVENT_CANCEL, EVENT_CANCEL_NOT_SELECT -> {
                if (selectMenuValue == null) {
                    setStageOfSettings(EventSettingsStatus.EVENT_CANCEL_NOT_SELECT, true, true);
                } else {
                    if (selectMenuValue.equals(VALUE_SELECT_MENU_OPTION_1)) {
                        isEndingEvent = true;
                        setStageOfSettings(EventSettingsStatus.SEND_NOTIFI, true, true);
                    } else if (selectMenuValue.equals(VALUE_SELECT_MENU_OPTION_2)) {
                        if (isChangedDateTime || isChangedName || isChangedDescription) {
                            setStageOfSettings(EventSettingsStatus.SEND_NOTIFI, true, true);
                        } else {
                            setStageOfSettings(EventSettingsStatus.END_NO_CHANGES, false, true);
                        }
                    }
                }
            }
            case SEND_NOTIFI, SEND_NOTIFI_NOT_SELECT -> {
                if (selectMenuValue == null) {
                    setStageOfSettings(EventSettingsStatus.SEND_NOTIFI_NOT_SELECT, true, true);
                } else {
                    setStageOfSettings(EventSettingsStatus.END, false, true);
                    if (selectMenuValue.equals(VALUE_SELECT_MENU_OPTION_1)) {
                        sendNotifi = true;
                    } else if (selectMenuValue.equals(VALUE_SELECT_MENU_OPTION_2)) {
                        sendNotifi = false;
                    }
                    endingEditor();
                    eventsSettingsService.removeSettingsPanel(userId);
                }
            }
            case DATE_TIME_TITLE_DESC_NOT_CHANGED ->
                    setStageOfSettings(EventSettingsStatus.END_NO_CHANGES, false, true);
            default -> throw new IllegalStageException(stageOfSettings);
        }
        selectMenuValue = null;
    }

    private void buttonBack() {
        switch (stageOfSettings) {
            case START -> {
                message.delete().queue();
                eventsSettingsService.removeSettingsPanel(userId);
            }
            case WHAT_TO_DO, DATE_TIME_NOT_CORRECT, DATE_TIME_TITLE_DESC_CHANGED, END, END_NO_CHANGES -> {
                activeEvents = eventService.findByIsActive();
                if (activeEvents.isEmpty()) {
                    setStageOfSettings(EventSettingsStatus.NO_EVENTS, false, false);
                } else {
                    event = null;
                    description = "";
                    isChangedDescription = false;
                    isChangedName = false;
                    isChangedDateTime = false;
                    isEndingEvent = false;
                    sendNotifi = false;
                    setStageOfSettings(EventSettingsStatus.START, true, true);
                }

            }
            case SEND_NOTIFI, DATE_TIME_TITLE_DESC_NOT_CHANGED, EVENT_CANCEL, EVENT_CANCEL_NOT_SELECT -> {
                selectMenuValue = null;
                setStageOfSettings(EventSettingsStatus.WHAT_TO_DO, true, true);

            }
            default -> throw new IllegalStageException(stageOfSettings);
        }
    }

    private void buttonCancel() {
        eventsSettingsService.removeSettingsPanel(userId);
        cancel();
    }

    private void setStageOfSettings(EventSettingsStatus stage, boolean withSelectMenu, boolean withButtons) {
        stageOfSettings = stage;
        if (withSelectMenu && withButtons) {
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
        } else if (withSelectMenu) {
            message.editMessageEmbeds(getEmbed()).setComponents(ActionRow.of(getSelectMenu())).queue();
        } else if (withButtons) {
            message.editMessageEmbeds(getEmbed()).setComponents(ActionRow.of(getButtons())).queue();
        } else {
            message.editMessageEmbeds(getEmbed()).setComponents().queue();
        }
    }

    public void submit(@NotNull ModalInteractionEvent event) {
        selectMenuValue = null;
        String modalId = event.getModalId();
        event.getInteraction().deferEdit().queue();
        switch (modalId) {
            case EVENT_SETTINGS_MODAL_TITLE -> saveTitleAndDesc(event);
            case EVENT_SETTINGS_MODAL_DATE_TIME -> saveDateTime(event);
            default -> throw new IllegalStateException("modalId=" + modalId);
        }
    }

    private void saveTitleAndDesc(@NotNull ModalInteractionEvent event) {
        String title = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_1)).getAsString();
        String desc = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_2)).getAsString();
        this.event.setName(title);
        description = desc;
        isChangedDescription = true;
        isChangedName = true;
        stageOfSettings = EventSettingsStatus.DATE_TIME_TITLE_DESC_CHANGED;
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
        log.info("Saved title and description");
    }

    private void saveDateTime(@NotNull ModalInteractionEvent event) {
        String date = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_1)).getAsString();
        String time = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_2)).getAsString();
        LocalDateTime dateTime = Converter.stringToLocalDateTime(date + " " + time);

        if (Validator.isDateTimeAfterNow(dateTime)) {
            this.event.setDate(dateTime);
            isChangedDateTime = true;
            stageOfSettings = EventSettingsStatus.DATE_TIME_TITLE_DESC_CHANGED;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            log.info("Saved date time");
        } else {
            stageOfSettings = EventSettingsStatus.DATE_TIME_NOT_CORRECT;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
            log.info("Date time not correct {}", dateTime);
        }

    }

    public String getUserId() {
        return userId;
    }

    private void endingEditor() {
        if (isEndingEvent) {
            eventService.cancelEvent(event, sendNotifi);
        } else {
            if (isChangedDateTime || isChangedName || isChangedDescription) {
                eventService.updateEmbed(event,
                        isChangedDateTime,
                        isChangedName,
                        isChangedDescription,
                        description,
                        sendNotifi);
            }
        }
    }

    private @NotNull User getUserById(String userId) {
        User user = DiscordBot.getJda().getUserById(userId);
        if (user != null) {
            return user;
        } else {
            throw new IllegalArgumentException("User is not exist");
        }
    }

    public void cancel() {
        setStageOfSettings(EventSettingsStatus.CANCEL, false, false);
    }

}

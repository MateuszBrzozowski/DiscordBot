package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.exceptions.IllegalStageException;
import pl.mbrzozowski.ranger.exceptions.StageNoSupportedException;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.helpers.Constants;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.helpers.Validator;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;

import static pl.mbrzozowski.ranger.guild.ComponentId.*;

@Slf4j
public class EventsGenerator {

    private final String TEXT_INPUT_ID_1 = "textInputId1";
    private final String TEXT_INPUT_ID_2 = "textInputId2";
    private final EventRequest eventRequest = new EventRequest();
    private Message message;
    private String selectMenuValue = "";
    private final EventService eventService;
    private final EventsGeneratorService eventsGeneratorService;
    private EventGeneratorStatus stageOfGenerator = EventGeneratorStatus.SET_NAME;

    @Autowired
    public EventsGenerator(@NotNull MessageReceivedEvent event,
                           EventService eventService,
                           EventsGeneratorService eventsGeneratorService) {
        this.eventService = eventService;
        this.eventsGeneratorService = eventsGeneratorService;
        eventRequest.setAuthorId(event.getMessage().getAuthor().getId());
        eventRequest.setAuthorName(event.getMessage().getAuthor().getName());
        start();
    }

    public EventsGenerator(@NotNull SlashCommandInteractionEvent event,
                           EventService eventService,
                           EventsGeneratorService eventsGeneratorService) {
        this.eventService = eventService;
        this.eventsGeneratorService = eventsGeneratorService;
        eventRequest.setAuthorId(event.getUser().getId());
        eventRequest.setAuthorName(event.getUser().getName());
        start();
    }

    private void start() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getButtons())).queue(message -> this.message = message));
    }

    @NotNull
    private SelectMenu getSelectMenu() {
        switch (stageOfGenerator) {
            case SET_PERMISSION, FINISH, FINISH_TIME_EXCEPTION, FINISH_EVENT_FOR_NOT_SELECTED -> {
                return StringSelectMenu
                        .create(EVENT_GENERATOR_SELECT_MENU_PERM)
                        .setPlaceholder("Dla kogo są zapisy")
                        .addOptions(EventFor.getAll())
                        .build();
            }
            default -> throw new IllegalStageException(stageOfGenerator);
        }
    }

    @NotNull
    private Collection<? extends ItemComponent> getButtons() {
        List<Button> buttons = new ArrayList<>();
        switch (stageOfGenerator) {
            case SET_NAME -> {
                buttons.add(Button.primary(EVENT_GENERATOR_BTN_BACK, "⮜ Wstecz").asDisabled());
                buttons.add(Button.primary(EVENT_GENERATOR_MODAL_TITLE, "Podaj nazwę ⮞"));
            }
            case SET_DATE_TIME, DATE_TIME_NOT_CORRECT -> {
                buttons.add(Button.primary(EVENT_GENERATOR_BTN_BACK, "⮜ Wstecz"));
                buttons.add(Button.primary(EVENT_GENERATOR_MODAL_TIME, "Podaj date i czas ⮞"));
            }
            case SET_PERMISSION, FINISH, FINISH_TIME_EXCEPTION, FINISH_EVENT_FOR_NOT_SELECTED -> {
                buttons.add(Button.primary(EVENT_GENERATOR_BTN_BACK, "⮜ Wstecz"));
                buttons.add(Button.success(EVENT_GENERATOR_BTN_NEXT, "Zakończ i udostępnij ⮞"));
            }
            default -> {
                buttons.add(Button.primary(EVENT_GENERATOR_BTN_BACK, "⮜ Wstecz"));
                buttons.add(Button.primary(EVENT_GENERATOR_BTN_NEXT, "Dalej ⮞"));
            }
        }
        buttons.add(Button.danger(EVENT_GENERATOR_BTN_CANCEL, "Przerwij"));
        return buttons;
    }

    @NotNull
    private MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setDescription("## :calendar_spiral: Wydarzenia - Generator :calendar_spiral: ");
        switch (stageOfGenerator) {
            case SET_NAME -> builder.addField("", "- Podaj nazwę\n- Podaj opis (opcjonalnie)", false);
            case SET_DATE_TIME -> {
                builder.addField(eventRequest.getName(), eventRequest.getDescription(), false);
                builder.addField("", "===============", false);
                builder.addField("", "- Podaj date i czas zakończenia", false);
            }
            case DATE_TIME_NOT_CORRECT -> {
                builder.setColor(Color.RED);
                builder.addField(eventRequest.getName(), eventRequest.getDescription(), false);
                builder.addField("", "===============", false);
                builder.addField("", "- Podaj date i czas zakończenia", false);
                builder.addField("Błąd", "- Data lub czas niepoprawny", false);
            }
            case SET_PERMISSION, FINISH -> {
                builder.addField(eventRequest.getName(), eventRequest.getDescription(), false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(eventRequest.getDateTime()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(eventRequest.getDateTime()),
                        true);
                builder.addField("", "===============", false);
                builder.addField("", "- Dla kogo są zapisy?", false);
            }
            case FINISH_TIME_EXCEPTION -> {
                builder.setColor(Color.RED);
                builder.addField(eventRequest.getName(), eventRequest.getDescription(), false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(eventRequest.getDateTime()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(eventRequest.getDateTime()),
                        false);
                builder.addField("", "===============", false);
                builder.addField("", "- Dla kogo są zapisy?", false);
                builder.addField("BŁĄD", "- Czas eventu nie może być data z przeszłości", false);
            }
            case FINISH_EVENT_FOR_NOT_SELECTED -> {
                builder.setColor(Color.RED);
                builder.addField(eventRequest.getName(), eventRequest.getDescription(), false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(eventRequest.getDateTime()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(eventRequest.getDateTime()),
                        false);
                builder.addField("", "===============", false);
                builder.addField("", "- Dla kogo są zapisy?", false);
                builder.addField("BŁĄD", "- Nie wybrano adresatów eventu", false);
            }
            case CANCEL -> {
                builder.setColor(Color.DARK_GRAY);
                builder.addField("", "Przerwano generowanie wydarzenia", false);
            }
            case END -> {
                builder.setColor(Color.GREEN);
                builder.addField(eventRequest.getName(), eventRequest.getDescription(), false);
                builder.addField(EmbedSettings.WHEN_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(eventRequest.getDateTime()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(eventRequest.getDateTime()),
                        true);
                builder.addBlankField(false);
                builder.addField("GENEROWANIE LISTY ZAKOŃCZONE", "Wydarzenie pojawi się na serwerze ", false);
            }
            default -> throw new IllegalStateException("Incorrect stage - " + stageOfGenerator);
        }
        return builder.build();
    }

    public void buttonEvent(@NotNull ButtonInteractionEvent event) {
        if (!event.getMessage().equals(message)) {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            return;
        }
        String componentId = event.getComponentId();
        log.info("Stage={}, buttonId={}", stageOfGenerator, componentId);
        switch (componentId) {
            case EVENT_GENERATOR_BTN_BACK -> {
                event.deferEdit().queue();
                buttonBack();
            }
            case EVENT_GENERATOR_BTN_NEXT -> {
                event.deferEdit().queue();
                buttonNext();
            }
            case EVENT_GENERATOR_BTN_CANCEL -> {
                event.deferEdit().queue();
                cancel();
                eventsGeneratorService.removeGenerator(event.getUser().getId());
            }
            case EVENT_GENERATOR_MODAL_TITLE -> showModalToAddTitle(event);
            case EVENT_GENERATOR_MODAL_TIME -> showModalToAddTime(event);
            default -> throw new NoSuchElementException("No such button - " + event.getComponentId());
        }
    }

    private void buttonNext() {
        switch (stageOfGenerator) {
            case SET_PERMISSION, FINISH, FINISH_TIME_EXCEPTION, FINISH_EVENT_FOR_NOT_SELECTED -> end();
            default -> throw new StageNoSupportedException("Stage - " + stageOfGenerator);
        }
    }

    private void buttonBack() {
        switch (stageOfGenerator) {
            case SET_DATE_TIME, DATE_TIME_NOT_CORRECT -> {
                stageOfGenerator = EventGeneratorStatus.SET_NAME;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getButtons())).queue();
                log.info("Back to {}", stageOfGenerator);
            }
            case SET_PERMISSION, FINISH, FINISH_EVENT_FOR_NOT_SELECTED, FINISH_TIME_EXCEPTION -> {
                stageOfGenerator = EventGeneratorStatus.SET_DATE_TIME;
                message.editMessageEmbeds(getEmbed())
                        .setComponents(ActionRow.of(getButtons())).queue();
                log.info("Back to {}", stageOfGenerator);
            }
            default -> throw new StageNoSupportedException("Stage - " + stageOfGenerator);
        }
    }

    private void showModalToAddTime(@NotNull ButtonInteractionEvent event) {
        stageOfGenerator = EventGeneratorStatus.SET_DATE_TIME;
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getButtons())).queue();
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

        if (eventRequest.getDateTime() != null) {
            date = TextInput.create(TEXT_INPUT_ID_1, "Data", TextInputStyle.SHORT)
                    .setPlaceholder("DD.MM.YYYY")
                    .setValue(
                            eventRequest.getDateTime().getDayOfMonth() + "." +
                                    String.format("%02d", eventRequest.getDateTime().getMonthValue()) + "." +
                                    eventRequest.getDateTime().getYear())
                    .setRequiredRange(9, 10)
                    .setRequired(true)
                    .build();

            time = TextInput.create(TEXT_INPUT_ID_2, "Czas", TextInputStyle.SHORT)
                    .setPlaceholder("HH:mm")
                    .setValue(eventRequest.getDateTime().getHour() + ":" + String.format("%02d", eventRequest.getDateTime().getMinute()))
                    .setRequiredRange(4, 5)
                    .setRequired(true)
                    .build();
        }

        Modal modal = Modal.create(EVENT_GENERATOR_MODAL_TIME, "Data i czas")
                .addComponents(ActionRow.of(date), ActionRow.of(time))
                .build();
        event.replyModal(modal).queue();
        log.info("Opened modal to set time");
    }

    private void showModalToAddTitle(@NotNull ButtonInteractionEvent event) {
        stageOfGenerator = EventGeneratorStatus.SET_NAME;
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getButtons())).queue();
        TextInput title = TextInput.create(TEXT_INPUT_ID_1, "Nazwa", TextInputStyle.SHORT)
                .setPlaceholder("Podaj nazwę eventu")
                .setRequiredRange(1, 256)
                .setRequired(true)
                .build();

        if (StringUtils.isNotBlank(eventRequest.getName())) {
            title = TextInput.create(TEXT_INPUT_ID_1, "Nazwa", TextInputStyle.SHORT)
                    .setPlaceholder("Podaj nazwę eventu")
                    .setValue(eventRequest.getName())
                    .setRequiredRange(1, 256)
                    .setRequired(true)
                    .build();
        }

        TextInput desc = TextInput.create(TEXT_INPUT_ID_2, "Opis", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Podaj opis eventu (opcjonalnie)")
                .setRequired(false)
                .setMaxLength(4000)
                .build();

        if (StringUtils.isNotBlank(eventRequest.getDescription())) {
            desc = TextInput.create(TEXT_INPUT_ID_2, "Opis", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Podaj opis eventu (opcjonalnie)")
                    .setValue(eventRequest.getDescription())
                    .setRequired(false)
                    .setMaxLength(4000)
                    .build();
        }

        Modal modal = Modal.create(ComponentId.EVENT_GENERATOR_MODAL_TITLE, "Nazwa i opis")
                .addComponents(ActionRow.of(title), ActionRow.of(desc))
                .build();
        event.replyModal(modal).queue();
        log.info("Opened modal to set title");
    }

    public void selectAnswer(@NotNull StringSelectInteractionEvent event) {
        event.deferEdit().queue();
        if (!event.getMessage().equals(message)) {
            event.getMessage().delete().queue();
            return;
        }
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        selectMenuValue = selectedOptions.get(0).getValue();
        if (selectMenuValue.equalsIgnoreCase(EventFor.CLAN_MEMBER.getValue())) {
            eventRequest.setEventFor(EventFor.CLAN_MEMBER);
        } else if (selectMenuValue.equalsIgnoreCase(EventFor.RECRUIT.getValue())) {
            eventRequest.setEventFor(EventFor.RECRUIT);
        } else if (selectMenuValue.equalsIgnoreCase(EventFor.CLAN_MEMBER_AND_RECRUIT.getValue())) {
            eventRequest.setEventFor(EventFor.CLAN_MEMBER_AND_RECRUIT);
        } else if (selectMenuValue.equalsIgnoreCase(EventFor.TACTICAL_GROUP.getValue())) {
            eventRequest.setEventFor(EventFor.TACTICAL_GROUP);
        } else if (selectMenuValue.equalsIgnoreCase(EventFor.SQ_EVENTS.getValue())) {
            eventRequest.setEventFor(EventFor.SQ_EVENTS);
        } else if (selectMenuValue.equalsIgnoreCase(EventFor.CLAN_COUNCIL.getValue())) {
            eventRequest.setEventFor(EventFor.CLAN_COUNCIL);
        } else {
            throw new IllegalStageException("Value of select menu - " + selectMenuValue);
        }
        stageOfGenerator = EventGeneratorStatus.FINISH;
        log.info("Saved selected option");
    }

    private void end() {
        if (!EventFor.isByValue(selectMenuValue)) {
            stageOfGenerator = EventGeneratorStatus.FINISH_EVENT_FOR_NOT_SELECTED;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            log.info("Can not end - stage={}", stageOfGenerator);
            return;
        }
        if (eventRequest.getDateTime().isBefore(LocalDateTime.now(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)))) {
            stageOfGenerator = EventGeneratorStatus.FINISH_TIME_EXCEPTION;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
            log.info("Can not end - stage={}", stageOfGenerator);
            return;
        }
        log.info("{}", eventRequest);
        eventService.createNewEvent(eventRequest);
        stageOfGenerator = EventGeneratorStatus.END;
        message.editMessageEmbeds(getEmbed()).setComponents().queue();
        eventsGeneratorService.removeGenerator(eventRequest.getAuthorId());
        log.info("Generator ended");
    }

    private @NotNull User getUserById(String userId) {
        User user = DiscordBot.getJda().getUserById(userId);
        if (user != null) {
            return user;
        } else {
            throw new IllegalArgumentException("User is not exist");
        }
    }

    public void submit(@NotNull ModalInteractionEvent event) {
        selectMenuValue = null;
        eventRequest.setEventFor(null);
        String modalId = event.getModalId();
        event.getInteraction().deferEdit().queue();
        switch (modalId) {
            case EVENT_GENERATOR_MODAL_TITLE -> saveTitleAndDescription(event);
            case EVENT_GENERATOR_MODAL_TIME -> saveDateAndTime(event);
            default -> throw new IllegalStageException(stageOfGenerator);
        }
    }

    private void saveTitleAndDescription(@NotNull ModalInteractionEvent event) {
        String title = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_1)).getAsString();
        String desc = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_2)).getAsString();
        eventRequest.setName(title);
        eventRequest.setDescription(desc);
        stageOfGenerator = EventGeneratorStatus.SET_DATE_TIME;
        message.editMessageEmbeds(getEmbed())
                .setComponents(ActionRow.of(getButtons())).queue();
        log.info("Saved title and description");
    }

    private void saveDateAndTime(@NotNull ModalInteractionEvent event) {
        String date = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_1)).getAsString();
        String time = Objects.requireNonNull(event.getValue(TEXT_INPUT_ID_2)).getAsString();
        LocalDateTime dateTime = Converter.stringToLocalDateTime(date + " " + time);
        if (Validator.isDateTimeAfterNow(dateTime)) {
            eventRequest.setDateTime(dateTime);
            stageOfGenerator = EventGeneratorStatus.SET_PERMISSION;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getSelectMenu()), ActionRow.of(getButtons())).queue();
        } else {
            eventRequest.setDateTime(null);
            stageOfGenerator = EventGeneratorStatus.DATE_TIME_NOT_CORRECT;
            message.editMessageEmbeds(getEmbed())
                    .setComponents(ActionRow.of(getButtons())).queue();
        }
        log.info("Saved time");
    }

    void cancel() {
        stageOfGenerator = EventGeneratorStatus.CANCEL;
        message.editMessageEmbeds(getEmbed()).setComponents().queue();
    }

    String getUserID() {
        return eventRequest.getAuthorId();
    }
}

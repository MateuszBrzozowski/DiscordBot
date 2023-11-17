package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.exceptions.IllegalStageException;
import pl.mbrzozowski.ranger.helpers.Validator;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EventsSettings {

    private final EventService eventService;
    private final JDA jda = DiscordBot.getJda();
    private final String userName;
    private final String userID;
    private final EventsSettingsService eventsSettingsService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy H:m");

    private Event event;
    private String description = "";
    private List<Event> eventsList = new ArrayList<>();
    private EventSettingsStatus stageOfSettings = EventSettingsStatus.CHOOSE_EVENT;
    private boolean isChangedDateTime = false;
    private boolean isChangedName = false;
    private boolean isChangedDescription = false;
    private boolean ifEndingEvent = false;
    private boolean sendNotifi = false;


    public EventsSettings(EventService eventService,
                          @NotNull MessageReceivedEvent privateEvent,
                          EventsSettingsService eventsSettingsService) {
        this.eventService = eventService;
        this.userID = privateEvent.getAuthor().getId();
        this.userName = privateEvent.getMessage().getAuthor().getName();
        this.eventsSettingsService = eventsSettingsService;
        embedStart();
    }

    private void embedStart() {
        User user = jda.getUserById(userID);
        if (user != null) {
            eventsList = eventService.findByIsActive();
            embedStart(user);
        }
    }

    @NotNull
    private String getActiveEventsIndexAndName() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < eventsList.size(); i++) {
            result.append(i + 1).append(" : ").append(eventsList.get(i).getName()).append("\n");
        }
        return result.toString();
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(@NotNull MessageReceivedEvent messageReceivedEvent) {
        String msg = messageReceivedEvent.getMessage().getContentDisplay();
        switch (stageOfSettings) {
            case CHOOSE_EVENT -> {
                int msgInteger;
                try {
                    msgInteger = Integer.parseInt(msg);
                } catch (NumberFormatException ex) {
                    messageReceivedEvent.getMessage().reply("Nieprawidłowy numer!").queue();
                    break;
                }
                if (msgInteger > 0 && msgInteger <= eventsList.size()) {
                    int selectedIndexOFEvent = msgInteger - 1;
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    event = eventsList.get(selectedIndexOFEvent);
                    embedWhatToDo();
                    log.info("{} - selected event({})", messageReceivedEvent.getAuthor(), eventsList.get(selectedIndexOFEvent).getName());
                } else {
                    messageReceivedEvent.getMessage().reply("Nieprawidłowy numer!").queue();
                }
            }
            case WHAT_TO_DO -> {
                int msgInteger;
                try {
                    msgInteger = Integer.parseInt(msg);
                } catch (NumberFormatException e) {
                    messageReceivedEvent.getMessage().reply("Nie rozumiem!").queue();
                    break;
                }
                switch (msgInteger) {
                    case 1 -> {
                        embedGetTime();
                        stageOfSettings = EventSettingsStatus.SET_TIME;
                        log.info("{} - selected {}", messageReceivedEvent.getAuthor(), stageOfSettings);
                    }
                    case 2 -> {
                        embedGetDate();
                        stageOfSettings = EventSettingsStatus.SET_DATE;
                        log.info("{} - selected {}", messageReceivedEvent.getAuthor(), stageOfSettings);
                    }
                    case 3 -> {
                        embedGetName();
                        stageOfSettings = EventSettingsStatus.SET_NAME;
                        log.info("{} - selected {}", messageReceivedEvent.getAuthor(), stageOfSettings);
                    }
                    case 4 -> {
                        embedGetDescription();
                        stageOfSettings = EventSettingsStatus.SET_DESCRIPTION;
                        log.info("{} - selected {}", messageReceivedEvent.getAuthor(), stageOfSettings);
                    }
                    case 8 -> {
                        isChangedDateTime = true;
                        embedCancelEvent();
                        stageOfSettings = EventSettingsStatus.CANCEL_EVENT;
                        log.info("{} - selected {}", messageReceivedEvent.getAuthor(), stageOfSettings);
                    }
                    case 9 -> {
                        finishEditor();
                        log.info("{} - selected cancel events settings", messageReceivedEvent.getAuthor());
                    }
                    case 0 -> {
                        if (isChangedDateTime) {
                            stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                            embedSendNotifi();
                        } else {
                            endingEditor();
                        }
                        log.info("{} - selected close and save events settings", messageReceivedEvent.getAuthor());
                    }
                    default -> {
                        embedWrongWhatToDo();
                        embedWhatToDo();
                        log.info("{} - wrong answer", messageReceivedEvent.getAuthor());
                    }
                }
            }
            case SET_TIME -> {
                if (Validator.isTimeValid(msg)) {
                    LocalDateTime newDate = LocalDateTime.parse(
                            event.getDate().getDayOfMonth() + "." + event.getDate().getMonthValue() + "." +
                                    event.getDate().getYear() + " " + msg,
                            dateTimeFormatter);
                    boolean isTimeAfterNow = Validator.isEventDateTimeAfterNow(newDate);
                    if (isTimeAfterNow) {
                        isChangedDateTime = true;
                        event.setDate(newDate);
                        log.info("{} - set new event time", messageReceivedEvent.getAuthor());
                    } else {
                        embedTimeNotCorrect();
                        log.warn("{} - time({}) after now", messageReceivedEvent.getAuthor(), msg);
                    }
                } else {
                    embedTimeNotCorrect();
                    log.warn("{} - time({}) not correct", messageReceivedEvent.getAuthor(), msg);
                }
                stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                embedWhatToDo();
            }
            case SET_DATE -> {
                boolean isDateFormat = Validator.isDateValid(msg);
                if (isDateFormat) {
                    LocalDateTime newDate = LocalDateTime.parse(
                            msg + " " + event.getDate().getHour() + ":" + event.getDate().getMinute(),
                            dateTimeFormatter);
                    boolean isTimeAfterNow = Validator.isEventDateTimeAfterNow(newDate);
                    if (isTimeAfterNow) {
                        isChangedDateTime = true;
                        event.setDate(newDate);
                        log.info("{} - set new event date", messageReceivedEvent.getAuthor());
                    } else {
                        embedDateNotCorrect();
                        log.warn("{} - date({}) after now", messageReceivedEvent.getAuthor(), msg);
                    }
                } else {
                    embedDateNotCorrect();
                    log.warn("{} - date({}) not correct", messageReceivedEvent.getAuthor(), msg);
                }
                stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                embedWhatToDo();
            }
            case SET_NAME -> {
                if (msg.length() < MessageEmbed.TITLE_MAX_LENGTH && msg.length() > 0) {
                    event.setName(msg);
                    isChangedName = true;
                    log.info("{} - set new event name", messageReceivedEvent.getAuthor());
                } else {
                    embedGetNameCorrect();
                    log.warn("{} - title({}, length:{}) not correct", messageReceivedEvent.getAuthor(), msg, msg.length());
                }
                stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                embedWhatToDo();
            }
            case SET_DESCRIPTION -> {
                if (msg.length() < MessageEmbed.DESCRIPTION_MAX_LENGTH && msg.length() > 0) {
                    description = msg;
                    isChangedDescription = true;
                    log.info("{} - set new event description", messageReceivedEvent.getAuthor());
                } else {
                    embedGetDescriptionCorrect();
                    log.warn("{} - description({}, length:{}) not correct", messageReceivedEvent.getAuthor(), msg, msg.length());
                }
                stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                embedWhatToDo();
            }
            case CANCEL_EVENT -> {
                if (msg.equalsIgnoreCase("T")) {
                    ifEndingEvent = true;
                    stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                    embedSendNotifi();
                    log.info("{} - selected YES to canceling event {}", messageReceivedEvent.getAuthor(), event.getName());
                } else if (msg.equalsIgnoreCase("N")) {
                    ifEndingEvent = false;
                    embedWhatToDo();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    log.info("{} - selected NO to canceling event {}", messageReceivedEvent.getAuthor(), event.getName());
                } else {
                    embedAnswerNotCorrect();
                    embedWhatToDo();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    log.info("{} - Answer not correct", messageReceivedEvent.getAuthor());
                }
            }
            case SEND_NOTIFI -> {
                if (msg.equalsIgnoreCase("T")) {
                    sendNotifi = true;
                    endingEditor();
                    log.info("{} - selected YES to send notifi for event {}", messageReceivedEvent.getAuthor(), event.getName());
                } else if (msg.equalsIgnoreCase("N")) {
                    sendNotifi = false;
                    endingEditor();
                    log.info("{} - selected NO to send notifi for event {}", messageReceivedEvent.getAuthor(), event.getName());
                } else {
                    embedAnswerNotCorrect();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                    log.info("{} - answer not correct", messageReceivedEvent.getAuthor());
                }
            }
            case FINISH -> {
                if (msg.equalsIgnoreCase("T")) {
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                } else if (msg.equalsIgnoreCase("N")) {
                    stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                    embedSendNotifi();
                } else {
                    embedAnswerNotCorrect();
                    embedDoYouWantAnyChange();
                }
            }
            default -> {
                embedError();
                removeThisEditor();
                throw new IllegalStageException(messageReceivedEvent.getAuthor(), stageOfSettings);
            }
        }
    }

    private void embedError() {
        String t = "Błąd edytora.";
        String d = "Zamykam edytor.";
        embedPatternOneField(Color.RED, t, d);
    }

    private void endingEditor() {
        if (ifEndingEvent) {
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
        finishEditor();
    }

    private void finishEditor() {
        embedCloseEditor();
        removeThisEditor();
    }

    private void removeThisEditor() {
        int index = eventsSettingsService.userHaveActiveSettingsPanel(userID);
        if (index >= 0) {
            eventsSettingsService.removeSettingsPanel(index);
        }
    }

    private void embedGetNameCorrect() {
        embedPatternOneField(Color.RED, "Nieprawidłowa nazwa eventu", "");
    }

    private void embedGetDescriptionCorrect() {
        embedPatternOneField(Color.RED, "Nieprawidłowy opis eventu", "");
    }

    private void embedCloseEditor() {
        String title = "Zamykam edytor eventów.";
        embedPatternOneField(Color.RED, title, "");
    }

    private void embedDoYouWantAnyChange() {
        String title = "Czy chcesz wprowadzić jakieś zmiany do eventu?";
        String description = "T - Tak\nN - Nie";
        embedPatternOneField(Color.GREEN, title, description);
    }

    private void embedAnswerNotCorrect() {
        String title = "Odpowiedź niepoprawna.";
        embedPatternOneField(Color.RED, title, "");
    }

    private void embedSendNotifi() {
        String title = "Czy wysłać powiadomienia do wszystkich zapisanych?";
        String description = "T - Tak\nN - Nie";
        embedPatternOneField(Color.GREEN, title, description);
    }

    private void embedTimeNotCorrect() {
        String title = "Nieprawidłowy wprowadzony czas.";
        String description = "Format: hh:mm";
        embedPatternOneField(Color.RED, title, description);
    }

    private void embedDateNotCorrect() {
        String title = "Nieprawidłowa wprowadzona data.";
        String description = "Format: dd.mm.yyyy";
        embedPatternOneField(Color.RED, title, description);
    }

    private void embedWrongWhatToDo() {
        String title = "Wprowadzono błędną wartość.";
        String description = "Wprowadź cyfrę z przed opisu zadania które chcesz wykonać.";
        embedPatternOneField(Color.RED, title, description);
    }

    private void embedCancelEvent() {
        String title = "Czy jestś pewien że chcesz odwołać event?";
        String description = """
                !!!UWAGA!!! - Odwołujesz event. Nie będzie możliwości odwrotu tego polecenia.
                Nie będzie można aktywować zamkniętego eventu i zapisów na niego. Bądź pewny tego ruchu.

                T - Tak
                N - Nie""";
        embedPatternOneField(Color.RED, title, description);
    }


    private void embedGetName() {
        String title = "Podaj nową nazwę eventu";
        String description = "Maksymalna liczba znaków - 256";
        embedPatternOneField(Color.YELLOW, title, description);
    }

    private void embedGetDescription() {
        String title = "Podaj nowy opis eventu";
        String description = "Maksymalna liczba znaków - 2048";
        embedPatternOneField(Color.YELLOW, title, description);
    }

    private void embedGetDate() {
        String title = "Podaj nową datę dla eventu.";
        String description = "Format: DD.MM.YYYY";
        embedPatternOneField(Color.YELLOW, title, description);
    }

    private void embedGetTime() {
        String title = "Podaj nowy czas rozpoczęcia dla eventu.";
        String description = "Format: hh:mm";
        embedPatternOneField(Color.YELLOW, title, description);
    }

    private void embedWhatToDo() {
        String title = "Co chcesz zrobić?";
        String description = """
                1 - Zmień godzinę
                2 - Zmień datę
                3 - Zmień nazwę
                4 - Zmień opis
                8 - Odwołaj event
                9 - Anuluj zmiany i zakończ edytowanie
                0 - Zapisz i zakończ edytowanie.""";
        embedPatternOneField(Color.YELLOW, title, description);
    }


    /**
     * @param color       Kolor Embed - RED - Bład, YELLOW - Następny krok lub informacja
     * @param title       Tytuł pola w embed.
     * @param description Opis pola w embed
     */
    private void embedPatternOneField(Color color, String title, String description) {
        User user = jda.getUserById(userID);
        if (user != null) {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(color);
                builder.addField(title, description, false);
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        } else {
            finishEditor();
            log.info("User is not exist");
            throw new IllegalArgumentException("User is not exist");
        }
    }

    private void embedStart(@NotNull User user) {
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("MENADŻER EVENTÓW");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setDescription("Cześć " + userName.toUpperCase() + ".\n" +
                    "Wybierz event który chcesz edytować.\n\n" +
                    getActiveEventsIndexAndName());
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    public boolean isPossiblyEditing() {
        return true;
    }
}

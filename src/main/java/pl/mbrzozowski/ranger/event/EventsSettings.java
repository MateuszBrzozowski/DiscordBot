package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.embed.EmbedSettings;
import pl.mbrzozowski.ranger.helpers.Validation;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventsSettings {

    private final EventService eventService;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy H:m");
    private boolean isChanged = false;
    private final JDA jda = DiscordBot.getJda();
    private final String userName;
    private final String userID;
    private Event event;
    private boolean ifEndingEvent = false;
    private boolean sendNotifi = false;
    private List<Event> eventsList = new ArrayList<>();
    private final EventsSettingsService eventsSettingsService;

    private EventSettingsStatus stageOfSettings = EventSettingsStatus.CHOOSE_EVENT;

    public EventsSettings(EventService eventService,
                          MessageReceivedEvent privateEvent,
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
            eventsList = eventService.findAll();
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
    }

    public String getActiveEventsIndexAndName() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < eventsList.size(); i++) {
            result.append(i + 1).append(" : ").append(eventsList.get(i).getName()).append("\n");
        }
        return result.toString();
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(MessageReceivedEvent messageReceivedEvent) {
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
                    int chossedIndexOFEvent = msgInteger - 1;
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    event = eventsList.get(chossedIndexOFEvent);
                    embedWhatToDo();
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
                        isChanged = true;
                        embedGetTime();
                        stageOfSettings = EventSettingsStatus.SET_TIME;
                    }
                    case 2 -> {
                        isChanged = true;
                        embedGetDate();
                        stageOfSettings = EventSettingsStatus.SET_DATE;
                    }
                    case 3 -> {
                        isChanged = true;
                        embedCancelEvent();
                        stageOfSettings = EventSettingsStatus.CANCEL_EVENT;
                    }
                    case 0 -> {
                        if (isChanged) {
                            stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                            embedSendNotifi();
                        } else {
                            endingEditor();
                        }
                    }
                    default -> {
                        embedWrongWhatToDo();
                        embedWhatToDo();
                    }
                }
            }
            case SET_TIME -> {
                msg = Validation.timeCorrect(msg);
                boolean isTimeFormat = Validation.isTimeFormat(msg);
                if (isTimeFormat) {
                    LocalDateTime newDate = LocalDateTime.parse(
                            event.getDate().getDayOfMonth() + "." + event.getDate().getMonthValue() + "." +
                                    event.getDate().getYear() + " " + msg,
                            dateTimeFormatter);
                    boolean isTimeAfterNow = Validation.eventDateAfterNow(newDate);
                    if (isTimeAfterNow) {
                        stageOfSettings = EventSettingsStatus.FINISH;
                        event.setDate(newDate);
                        embedDoYouWantAnyChange();
                    } else {
                        embedTimeNotCorrect();
                    }
                } else {
                    embedTimeNotCorrect();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                }
            }
            case SET_DATE -> {
                boolean isDateFormat = Validation.isDateFormat(msg);
                if (isDateFormat) {
                    LocalDateTime newDate = LocalDateTime.parse(
                            msg + " " + event.getDate().getHour() + ":" + event.getDate().getMinute(),
                            dateTimeFormatter);
                    boolean isTimeAfterNow = Validation.eventDateAfterNow(newDate);
                    if (isTimeAfterNow) {
                        stageOfSettings = EventSettingsStatus.FINISH;
                        event.setDate(newDate);
                        embedDoYouWantAnyChange();
                    } else {
                        embedDateNotCorrect();
                    }
                } else {
                    embedDateNotCorrect();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                }
            }
            case CANCEL_EVENT -> {
                if (msg.equalsIgnoreCase("T")) {
                    ifEndingEvent = true;
                    stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                    embedSendNotifi();
                } else if (msg.equalsIgnoreCase("N")) {
                    ifEndingEvent = false;
                    embedDoYouWantAnyChange();
                    stageOfSettings = EventSettingsStatus.FINISH;
                } else {
                    embedAnswerNotCorrect();
                    embedCancelEvent();
                }
            }
            case SEND_NOTIFI -> {
                if (msg.equalsIgnoreCase("T")) {
                    sendNotifi = true;
                    endingEditor();
                } else if (msg.equalsIgnoreCase("N")) {
                    sendNotifi = false;
                    endingEditor();
                } else {
                    embedAnswerNotCorrect();
                    embedSendNotifi();
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
            if (sendNotifi) {
                eventService.cancelEventWithInfoForPlayers(event);
            } else {
                eventService.cancelEvent(event);
            }
        } else {
            if (isChanged) {
                eventService.changeDateAndTime(event, sendNotifi);
            }
        }
        embedCloseEditor();
        removeThisEditor();
    }

    private void removeThisEditor() {
        int index = eventsSettingsService.userHaveActiveSettingsPanel(userID);
        if (index >= 0) {
            eventsSettingsService.removeSettingsPanel(index);
        }
    }

    private void embedCloseEditor() {
        String d = "Zamykam edytor eventów.";
        embedPatternOneField(Color.RED, "", d);
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
        String title = "Czy jestś pewien że chcesz zakończyć event?";
        String description = """
                !!!UWAGA!!! - Zamyka event i usuwa z bazy danych. Nie będzie możliwości odwrotu tego polecenia.
                Nie będzie można aktywować zamkniętego eventu i zapisów na niego. Bądź pewny tego ruchu.

                T - Tak
                N - Nie""";
        embedPatternOneField(Color.RED, title, description);
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
                3 - Odwołaj event
                0 - Zakończ edytowanie.""";
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
        }
    }


    public boolean isPossiblyEditing() {
        return true;
    }
}

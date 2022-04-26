package event;

import embed.EmbedSettings;
import helpers.RangerLogger;
import helpers.Validation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventsSettings {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private boolean possiblyEditing = true;
    private JDA jda = Repository.getJda();
    private Event event = Repository.getEvent();
    private final String userName;
    private final String userID;
    private List<String> indexsWithAllEventsID = new ArrayList<>();
    private int chossedIndexOFEvent;
    private String date;
    private String newDate;
    private String time;
    private String newTime;
    private boolean ifEndingEvent = false;
    private boolean sendNotifi = false;

    private EventSettingsStatus stageOfSettings = EventSettingsStatus.CHOOSE_EVENT;

    public EventsSettings(MessageReceivedEvent privateEvent) {
        this.userID = privateEvent.getAuthor().getId();
        this.userName = privateEvent.getMessage().getAuthor().getName();
        embedStart();
    }

    private void embedStart() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("MENADŻER EVENTÓW");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setDescription("Cześć " + userName.toUpperCase() + ".\n" +
                    "Wybierz event który chcesz edytować.\n\n" +
                    event.getActiveEventsIndexAndName());
            privateChannel.sendMessageEmbeds(builder.build()).queue();
            indexsWithAllEventsID = event.getAllEventID();
        });
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(MessageReceivedEvent messageReceivedEvent) {
        String msg = messageReceivedEvent.getMessage().getContentDisplay();
        switch (stageOfSettings) {
            case CHOOSE_EVENT: {
                int msgInteger = 0;
                try {
                    msgInteger = Integer.parseInt(msg);
                } catch (NumberFormatException ex) {
                    logger.error(ex.getMessage());
                }
                if (msgInteger > 0 && msgInteger <= indexsWithAllEventsID.size()) {
                    chossedIndexOFEvent = msgInteger - 1;
                    if (event.checkEventIDOnIndex(chossedIndexOFEvent, indexsWithAllEventsID.get(chossedIndexOFEvent))) {
                        String eventIDLocal = indexsWithAllEventsID.get(chossedIndexOFEvent);
                        date = event.getDateFromEmbed(eventIDLocal);
                        time = event.getTimeFromEmbed(eventIDLocal);
                        newTime = time;
                        newDate = date;
                        stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                        embedWhatToDo();
                    } else {
                        possiblyEditing = false;
                        RangerLogger.info("Zmiany w eventach. Dalsze edytowanie niemożliwe. " +
                                "Prawdopodobnie dwóch użytkowników w tym samym czasie edytuje eventy.");
                        embedEditingNotPossible();
                    }
                } else {
                    embedWrongEventID();
                }
                break;
            }
            case WHAT_TO_DO: {
                int msgInteger = 99;
                try {
                    msgInteger = Integer.parseInt(msg);
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage());
                }
                switch (msgInteger) {
                    case 1:
                        embedGetTime();
                        stageOfSettings = EventSettingsStatus.SET_TIME;
                        break;
                    case 2:
                        embedGetDate();
                        stageOfSettings = EventSettingsStatus.SET_DATE;
                        break;
                    case 3:
                        embedCancelEvent();
                        stageOfSettings = EventSettingsStatus.CANCEL_EVENT;
                        break;
                    case 0:
                        if (checkChanges()) {
                            stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                            embedSendNotifi();
                        } else {
                            embedNoChanges();
                            removeThisEditor();
                        }
                        break;
                    default:
                        embedWrongWhatToDo();
                        embedWhatToDo();
                        break;
                }
                break;
            }
            case SET_TIME: {
                msg = Validation.timeCorrect(msg);
                boolean isTimeFormat = Validation.isTimeFormat(msg);
                boolean isTimeAfterNow = Validation.eventDateTimeAfterNow(date + " " + msg);
                if (isTimeFormat && isTimeAfterNow) {
                    stageOfSettings = EventSettingsStatus.FINISH;
                    newTime = msg;
                    embedDoYouWantAnyChange();
                } else {
                    embedTimeNotCorrect();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                }
                break;
            }
            case SET_DATE: {
                boolean isDateFormat = Validation.isDateFormat(msg);
                boolean isTimeAfterNow = Validation.eventDateTimeAfterNow(msg + " " + time);
                if (isDateFormat && isTimeAfterNow) {
                    stageOfSettings = EventSettingsStatus.FINISH;
                    newDate = msg;
                    embedDoYouWantAnyChange();
                } else {
                    embedDateNotCorrect();
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                }
                break;
            }
            case CANCEL_EVENT: {
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
                break;
            }
            case SEND_NOTIFI: {
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
                break;
            }
            case FINISH: {
                if (msg.equalsIgnoreCase("T")) {
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                } else if (msg.equalsIgnoreCase("N")) {
                    if (checkChanges()) {
                        stageOfSettings = EventSettingsStatus.SEND_NOTIFI;
                        embedSendNotifi();
                    } else {
                        embedNoChanges();
                        removeThisEditor();
                    }
                } else {
                    embedAnswerNotCorrect();
                    embedDoYouWantAnyChange();
                }
                break;
            }
            default: {
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
        String eventID = indexsWithAllEventsID.get(chossedIndexOFEvent);
        if (event.checkEventIDOnIndex(chossedIndexOFEvent, indexsWithAllEventsID.get(chossedIndexOFEvent))) {
            if (ifEndingEvent) {
                if (sendNotifi) {
                    event.cancelEvnetWithInfoForPlayers(eventID);
                } else {
                    event.cancelEvent(eventID);
                }
            } else {
                if (changedDate() && changedTime()) {
                    event.changeDateAndTime(eventID, newDate, newTime, userID, sendNotifi);
                } else if (changedDate()) {
                    event.changeDate(eventID, newDate, userID, sendNotifi);
                } else if (changedTime()) {
                    event.changeTime(eventID, newTime, userID, sendNotifi);
                }
            }
            embedCloseEditor();
        } else {
            possiblyEditing = false;
            removeThisEditor();
            embedNoPossibleEditig();
            RangerLogger.info("Zmiany w eventach. Dalsze edytowanie niemożliwe. " +
                    "Prawdopodobnie dwóch użytkowników w tym samym czasie edytuje eventy.");
            embedEditingNotPossible();
        }
    }

    private void embedNoPossibleEditig() {
        String t = "Zmiany niemożliwe do wprowadzenia.";
        String d = "Zamykam edytor eventów";
        embedPatternOneField(Color.RED, t, d);
    }

    private void removeThisEditor() {
        EventsSettingsModel model = Repository.getEventsSettingsModel();
        int index = model.userHaveActiveSettingsPanel(userID);
        if (index >= 0) {
            model.removeSettingsPanel(index);
        }
    }

    private void embedNoChanges() {
        String t = "Nie wprowadzono żadnych zmian.";
        String d = "Zamykam edytor eventów.";
        embedPatternOneField(Color.RED, t, d);
    }

    private void embedCloseEditor() {
        String d = "Zamykam edytor eventów.";
        embedPatternOneField(Color.RED, "", d);
    }

    /**
     * Sprawdza czy zostały wprowadzone zmiany w evencie.
     *
     * @return true - jeżeli zostały wprowadzone zmiany w czasie lub dacie lub event ma być anulowany. false - brak zmian
     */
    private boolean checkChanges() {
        if (changedTime() || changedDate() || ifEndingEvent) {
            return true;
        }
        return false;
    }

    private boolean changedDate() {
        if (date.equalsIgnoreCase(newDate)) {
            return false;
        }
        return true;
    }

    private boolean changedTime() {
        if (time.equalsIgnoreCase(newTime)) {
            return false;
        }
        return true;
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


    private void embedEditingNotPossible() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Dalsze edytowanie niemożliwe. Spróbuj ponownie za chwilę uruchomić edytor.");
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            privateChannel.sendMessageEmbeds(builder.build());
        });

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
        String description = "!!!UWAGA!!! - Zamyka event i usuwa z bazy danych. Nie będzie możliwości odwrotu tego polecenia. " +
                "Nie będzie można aktywować zamkniętego eventu i zapisów na niego. Bądź pewny tego ruchu.\n\n" +
                "T - Tak\nN - Nie";
        embedPatternOneField(Color.RED, title, description);
    }

    private void embedWhatMsgSend() {
        String title = "Podaj wiadomośći jaką chcesz wysłać do wszystkich zapisanych użytkowników.";
        embedPatternOneField(Color.YELLOW, title, "");
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

    private void embedWrongEventID() {
        String title = "Nieprawidłowy numer eventu. Wybierz event z podanych poniżej podając liczbę która jest przed nazwą.";
        embedPatternOneField(Color.RED, title, event.getActiveEventsIndexAndName());
    }

    private void embedWhatToDo() {
        String title = "Co chcesz zrobić?";
        String description = "1 - Zmień godzinę\n" +
                "2 - Zmień datę\n" +
                "3 - Anuluj event\n" +
                "0 - Zakończ edytowanie.";
        embedPatternOneField(Color.YELLOW, title, description);
    }


    /**
     * @param color       Kolor Embed - RED - Bład, YELLOW - Następny krok lub informacja
     * @param title       Tytuł pola w embed.
     * @param description Opis pola w embed
     */
    private void embedPatternOneField(Color color, String title, String description) {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(color);
            builder.addField(title, description, false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }


    public boolean isPossiblyEditing() {
        return possiblyEditing;
    }
}

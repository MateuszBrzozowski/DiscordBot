package event;

import embed.EmbedSettings;
import helpers.Validation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.awt.*;

public class EventsSettings {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private JDA jda = Repository.getJda();
    private Event event = Repository.getEvent();
    private final String userName;
    private final String userID;
    private String eventID;
    private String date;
    private String time;
    private int indexOFEvent;
    private final PrivateMessageReceivedEvent privateMsgEvent;
    private EventSettingsStatus stageOfSettings = EventSettingsStatus.CHOOSE_EVENT;

    public EventsSettings(PrivateMessageReceivedEvent privateEvent) {
        this.userID = privateEvent.getAuthor().getId();
        this.userName = privateEvent.getMessage().getAuthor().getName();
        this.privateMsgEvent = privateEvent;
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
            //TODO pobrać evnetID (przmeyslec jak przechowywac dane dwoch uzytkownikow na raz edytuje np)
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(PrivateMessageReceivedEvent privateMsgEvent) {
        String msg = privateMsgEvent.getMessage().getContentDisplay();
        switch (stageOfSettings) {
            case CHOOSE_EVENT: {
                int activeEventsListSize = event.getActiveEventsListSize();
                int msgInteger = 0;
                try {
                    msgInteger = Integer.parseInt(msg);
                } catch (NumberFormatException ex) {
                    logger.error(ex.getMessage());
                }

                if (msgInteger > 0 && msgInteger <= activeEventsListSize) {
                    indexOFEvent = msgInteger - 1;
                    //TODO pobrać date i time do zmiennej w klasie
                    stageOfSettings = EventSettingsStatus.WHAT_TO_DO;
                    embedWhatToDo();
                } else {
                    embedWrongEventID();
                }
                break;
            }
            case WHAT_TO_DO: {
                int msgInteger = 0;
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
                        embedWhatMsgSend();
                        stageOfSettings = EventSettingsStatus.SEND_MSG;
                        break;
                    case 4:
                        embedCancelEvent();
                        stageOfSettings = EventSettingsStatus.CANCEL_EVENT;
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

                } else {
                    embedTimeNotCorrect();
                }
                break;
            }
        }
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
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedTimeNotCorrect() {
        String title = "Nieprawidłowy wprowadzony czas.";
        String description = "Format: hh:mm";
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
                "Nie będzie można aktywować zamkniętego eventu i zapisów na niego. Bądź pewny tego ruchu.";
        embedPatternOneField(Color.RED, title, description);
    }

    private void embedWhatMsgSend() {
        String title = "Podaj wiadomośći jaką chcesz wysłać do wszystkich zapisanych użytkowników.";
        String description = "";
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

    private void embedWrongEventID() {
        String title = "Nieprawidłowy numer eventu. Wybierz event z podanych poniżej podając liczbę która jest przed nazwą.";
        embedPatternOneField(Color.RED, title, event.getActiveEventsIndexAndName());
    }

    private void embedWhatToDo() {
        String title = "Co chcesz zrobić?";
        String description = "1. Zmień godzinę\n" +
                "2. Zmień datę\n" +
                "3. Wyślij wiadomość do zapisanych.\n" +
                "4. Anuluj event";
        embedPatternOneField(Color.YELLOW, title, description);
    }


}

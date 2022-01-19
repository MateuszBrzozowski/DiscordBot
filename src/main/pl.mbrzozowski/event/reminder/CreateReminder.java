package event.reminder;

import event.Event;
import helpers.Validation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class CreateReminder {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DateFormat dateFormat = new SimpleDateFormat("d.MM.yyyy HH:mm");
    private static final String datePattern = "dd.MM.yyyy";
    private String date = "";
    private String time = "";
    private String eventID = "";

    /**
     * @param date    Data wydarzenia
     * @param time    Czas wydarzenia
     * @param eventID ID wiadomości w której jest lista.
     */
    public CreateReminder(String date, String time, String eventID) {
        this.date = date;
        this.time = time;
        this.eventID = eventID;
    }

    /**
     * @param eventID ID wiadomości, w której znajduję się lista z zapisami na event.
     */
    public CreateReminder(String eventID) {
        this.eventID = eventID;
    }

    public void create() {
        if (date != "" && time != "") {
            setReminder();
        } else {
            if (eventID != "") {
                setReminderFromEmbed();
            } else {
                logger.info("Brak danych do stworzenia remindera (brak daty/czasu/lub");
            }
        }
    }

    private void setReminder() {
        if (date != "" && time != "") {
            time = Validation.timeCorrect(time);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
            String dateTime = date + " " + time;
            LocalDateTime eventDateTime = null;
            try {
                eventDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
            Timer timerOneHour = new Timer();
            Timer timerOneDay = new Timer();
            if (eventDateTime != null) {
                LocalDateTime oneHourBefore = eventDateTime.minusHours(1);
                oneHourBefore.atZone(ZoneId.of("Europe/Paris"));
                LocalDateTime oneDayBefore = eventDateTime.minusDays(1);
                oneDayBefore.atZone(ZoneId.of("Europe/Paris"));

                setReminderWithExactTime(timerOneHour, oneHourBefore, TypeOfReminder.ONE_HOUR);
                setReminderWithExactTime(timerOneDay, oneDayBefore, TypeOfReminder.ONE_DAY);
            }
        }
    }

    private void setReminderWithExactTime(Timer timer, LocalDateTime timerTme, TypeOfReminder type) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        Date eventDateTime = Date.from(timerTme.atZone(ZoneId.of("Europe/Paris")).toInstant());
        if (dateTimeNow.isBefore(timerTme)) {
            timer.schedule(new Reminder(eventID, type), eventDateTime);
            logger.info("Ustawiam timer");

            MyTimer myTimer = new MyTimer(eventID, timer);
            Timers timers = Repository.getTimers();
            timers.add(myTimer);
        }
    }

    /**
     * Pobiera datę i godzinę z embed z wiadomości (listy) i ustawia reminder dla wydarzenia.
     */
    private void setReminderFromEmbed() {
        JDA jda = Repository.getJda();
        Event event = Repository.getEvent();
        String channelID = event.getChannelID(eventID);
        jda.getTextChannelById(channelID).retrieveMessageById(eventID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            List<MessageEmbed.Field> fields = embeds.get(0).getFields();
            date = fields.get(0).getValue();
            time = fields.get(2).getValue();
            setReminder();
        });
    }

}

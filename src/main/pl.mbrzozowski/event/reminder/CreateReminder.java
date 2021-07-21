package event.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class CreateReminder {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private String date = "";
    private String time = "";
    private String eventID = "";

    public static void main(String[] args) {
//        CreateReminder reminder = new CreateReminder("20.10.2010","19:00");
        CreateReminder reminder = new CreateReminder("123456789123456789");
        reminder.create();
    }

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
        if (!date.isBlank() && !time.isBlank()) {
            Date dateFull = null;
            try {
                dateFull = dateFormat.parse(date + " " + time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Timer timer = new Timer();
            if (dateFull != null) timer.schedule(new Reminder(eventID), dateFull);
        } else {
            if (!eventID.isBlank()) {
                //Pobrac dane z embed date i czas i wywolac jeszcze raz ta funkcje
//                create();
            } else {
                logger.info("Brak danych do stworzenia remindera (brak daty/czasu/lub");
            }
        }
    }
}

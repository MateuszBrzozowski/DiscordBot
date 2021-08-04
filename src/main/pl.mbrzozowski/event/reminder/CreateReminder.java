package event.reminder;

import event.Event;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;
import recrut.Recruits;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class CreateReminder {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DateFormat dateFormat = new SimpleDateFormat("d.MM.yyyy HH:mm");
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
            Date dateFull = null;
            try {
                dateFull = dateFormat.parse(date + " " + time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Timer timer = new Timer();
            if (dateFull != null) {
                dateFull = new Date(dateFull.getTime() - (1 * 60 * 1000)); //ustawia 15 minut przed wydarzenie
                Date nowDate = new Date();
                if (nowDate.before(dateFull)) {
                    timer.schedule(new Reminder(eventID), dateFull);
                    logger.info("Ustawiam timer");

                    MyTimer myTimer = new MyTimer(eventID, timer);
                    Timers timers = Repository.getTimers();
                    timers.add(myTimer);
                }
            }
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

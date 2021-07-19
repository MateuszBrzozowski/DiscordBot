package event;

import java.util.TimerTask;

public class Reminder extends TimerTask {

    private String eventID;

    /**
     * @param eventID - ID eventu, id wiadmości w której jest lista z zapisami.
     */
    public Reminder(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public void run() {
        //znaleźć index aktywnego eventu po id.
        //pobrac sobie wszystkich uzytkownikow z bazy danych ktorzy nie chca dostawac powiadomien.
        //przeleciec po kazdym w mainList i wyslac prywatna wiadmosc
        //przeleciec po kazdym z reserveList i wyslac wiadomosc.

    }

}

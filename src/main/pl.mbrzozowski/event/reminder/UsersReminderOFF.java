package event.reminder;

import database.DBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsersReminderOFF {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private List<String> usersOff = new ArrayList<>();

    public UsersReminderOFF() {
        ResultSet allUsers = getAllUsers();

        if (allUsers != null) {
            while (true) {
                try {
                    if (!allUsers.next()) break;
                    else {
                        String userID = allUsers.getString("userID");
                        logger.info("USER ID: " + userID);
                        usersOff.add(userID);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }
        logger.info("Wielkość listy: " + usersOff.size());
    }

    private ResultSet getAllUsers() {
        String query = "SELECT * FROM `reminderoff`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli reminderoff w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE reminderoff(userID VARCHAR(30) PRIMARY KEY)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    /**
     * @return Zwraca true jeżeli użytkownik ma wyłączony reminder i nie chce otrzymywać powiadomień.
     * W innym przypadku zwraca false
     * @param userID ID użytkownika którego sprawdzamy
     */
    public boolean userHasOff(String userID) {
        for (int i = 0; i < usersOff.size(); i++) {
            if (usersOff.get(i).equalsIgnoreCase(userID)){
                return true;
            }
        }
        return false;
    }

    public void add(String userID) {
        if (!userHasOff(userID)){
            String query = "INSERT INTO reminderoff(`userID`) VALUES (\"%s\")";
            DBConnector connector = new DBConnector();
            connector.executeQuery(String.format(query,userID));
            usersOff.add(userID);
        }
    }

    public void remove(String userID) {
        if (userHasOff(userID)){
            String query = "DELETE FROM reminderoff WHERE userID=\"%s\"";
            DBConnector connector = new DBConnector();
            connector.executeQuery(String.format(query,userID));
            usersOff.remove(getIndex(userID));
        }
    }

    private int getIndex(String userID) {
        for (int i = 0; i < usersOff.size(); i++) {
            if (usersOff.get(i).equalsIgnoreCase(userID)){
                return i;
            }
        }
        return -1;
    }
}

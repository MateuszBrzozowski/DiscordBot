package ranger.event;

import ranger.database.DBConnector;
import ranger.database.DBFactory;
import ranger.database.DBType;
import ranger.database.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class EventDatabase {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Factory factory = new DBFactory();
    private DBConnector connector = factory.createDB(DBType.RANGER);

    ResultSet getAllPlayers() {
        String query = "SELECT * FROM `players`";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli players w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE players(" +
                    "id INT(9) UNSIGNED AUTO_INCREMENT PRIMARY KEY, " +
                    "userID VARCHAR(30)," +
                    "userName VARCHAR(30) NOT NULL," +
                    "mainList BOOLEAN," +
                    "event VARCHAR(30) NOT NULL," +
                    "FOREIGN KEY (event) REFERENCES event(msgID))";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    ResultSet getAllEvents() {
        String query = "SELECT * FROM `event`";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli event w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE event(" +
                    "msgID VARCHAR(30) PRIMARY KEY," +
                    "channelID VARCHAR(30) NOT NULL)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    void removeEvent(String messageID) {
        String queryPlayers = "DELETE FROM players WHERE event=\"%s\"";
        connector.executeQuery(String.format(queryPlayers, messageID));
        String queryEvent = "DELETE FROM event WHERE msgID=\"%s\"";
        connector.executeQuery(String.format(queryEvent, messageID));
    }

    void addEvent(String channelID, String messageID) {
        String query = "INSERT INTO `event` (`channelID`,`msgID`) VALUES (\"%s\",\"%s\")";
        connector.executeQuery(String.format(query, channelID, messageID));
    }

    void addPlayer(String userID, String userName, boolean isMainList, String messageID) {
        String query = "INSERT INTO players (`userID`, `userName`, `mainList`, `event`) VALUES (\"%s\", \"%s\", %b, \"%s\")";
        connector.executeQuery(String.format(query, userID, userName, isMainList, messageID));
    }

    void removePlayer(String userID, String messageID) {
        String query = "DELETE FROM players WHERE userID=\"%s\" AND event=\"%s\"";
        connector.executeQuery(String.format(query, userID, messageID));
    }
}

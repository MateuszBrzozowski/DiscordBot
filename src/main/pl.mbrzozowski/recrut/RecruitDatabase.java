package recrut;

import database.DBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class RecruitDatabase {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DBConnector connector = new DBConnector();

    ResultSet getAllRecrut() {
        String query = "SELECT * FROM `recruts`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli recruts w bazie danych -> Tworze tabele.");
            String queryCreate = "CREATE TABLE recruts(" +
                    "userID VARCHAR(30) PRIMARY KEY," +
                    "userName VARCHAR(30) NOT NULL," +
                    "channelID VARCHAR(30) NOT NULL)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    void addUser(String userID, String userName, String channelID) {
        String query = "INSERT INTO `recruts` (`userID`, `userName`, `channelID`) VALUES (\"%s\",\"%s\",\"%s\")";
        connector.executeQuery(String.format(query, userID, userName, channelID));
    }

    void removeUser(String channelID){
        String query = "DELETE FROM `recruts` WHERE channelID=\"%s\"";
        connector.executeQuery(String.format(query, channelID));
    }
}

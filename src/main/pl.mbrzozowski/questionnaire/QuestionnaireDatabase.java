package questionnaire;

import database.DBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class QuestionnaireDatabase {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DBConnector connector = new DBConnector();


    ResultSet getAnswerIDFromDatabase(String messageID, String emojiID) {
        String query = "SELECT id FROM answers WHERE msgID=\"" + messageID + "\" AND emojiID=\"" + emojiID + "\"";
        ResultSet resultSet = null;
        resultSet = connector.executeSelect(query);
        return resultSet;
    }

    ResultSet getAllQuestionnaire() {
        String query = "SELECT * from `questionnaire`";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            createTableQuestionnaire();
            createTableAnswers();
            createTableUserAnswer();
        }
        return resultSet;
    }

    ResultSet getAllAnswersFromQuestionnaireID(String messageID) {
        String query = "SELECT * from `answers` WHERE msgID=\"" + messageID + "\"";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            createTableAnswers();
        }
        return resultSet;
    }

    ResultSet getAllUserAnswerWithID(int id) {
        String query = "SELECT * from `user_answer` WHERE answer_id=\"" + id + "\"";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            createTableUserAnswer();
        }
        return resultSet;
    }

    private void createTableQuestionnaire() {
        String queryCreate = "CREATE TABLE questionnaire(" +
                "msgID VARCHAR(30) PRIMARY KEY," +
                "channelID VARCHAR(30) NOT NULL," +
                "authorID VARCHAR(30) NOT NULL," +
                "isMultiple BOOLEAN," +
                "isPublic BOOLEAN)";
        createTable(queryCreate);
    }

    private void createTableAnswers() {
        String queryCreate = "CREATE TABLE answers(" +
                "id INT(9) UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "msgID VARCHAR(30) NOT NULL," +
                "answer MEDIUMTEXT," +
                "emojiID VARCHAR(30) NOT NULL," +
                "FOREIGN KEY (msgID) REFERENCES questionnaire(msgID))";
        createTable(queryCreate);
    }

    private void createTableUserAnswer() {
        String queryCreate = "CREATE TABLE user_answer(" +
                "id INT(9) UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "answer_id INT(9) UNSIGNED," +
                "userID VARCHAR(30)," +
                "FOREIGN KEY (answer_id) REFERENCES answers(id))";
        createTable(queryCreate);
    }

    private void createTable(String queryCreate) {
        connector.executeQuery(queryCreate);
    }

    public void addUserToAnswer(int idDb, String userID) {
        String query = "INSERT INTO user_answer (`answer_id`,`userID`) " +
                "VALUES (\"" + idDb + "\",\"" + userID + "\")";
        connector.executeQuery(query);
    }

    public void removeUserAnswer(int idAnswer, String userID) {
        String query = "DELETE FROM user_answer WHERE answer_id=\"" + idAnswer + "\" AND userID=\"" + userID + "\"";
        connector.executeQuery(query);
    }
}

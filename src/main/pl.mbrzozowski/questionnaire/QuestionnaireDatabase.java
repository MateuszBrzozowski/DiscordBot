package questionnaire;

import database.DBConnector;

import java.sql.ResultSet;

public class QuestionnaireDatabase {

    private DBConnector connector = new DBConnector();

//    public QuestionnaireDatabase() {
//        createTableQuestionnaire();
//        createTableAnswers();
//        createTableUserAnswer();
//    }

    ResultSet getAllQuestionnaire() {
        String query = "SELECT * from `questionnaire`";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            createTableQuestionnaire();
        }
        return resultSet;
    }

    ResultSet getAllAnswers() {
        String query = "SELECT * from `answers`";
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            createTableAnswers();
        }
        return resultSet;
    }

    ResultSet getAllUserAnswer() {
        String query = "SELECT * from `user_answer`";
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
                "answerID VARCHAR(30) NOT NULL," +
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
}

package questionnaire;

import database.DBConnector;
import database.DBFactory;
import database.DBType;
import database.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class QuestionnaireDatabase {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Factory factory = new DBFactory();
    private DBConnector connector = factory.createDB(DBType.RANGER);


    /**
     * Pobiera ID wszystkich odpowiedzi dla ankiety o id przekazanej w parametrze
     *
     * @param messageID ID wiadomości w której jest jest ankieta
     * @return Wszystkie odpowiedzi z tabeli answers dla ankiety o ID messageID
     */
    ResultSet getAnswerIDFromDatabase(String messageID) {
        String query = "SELECT * FROM `answers` WHERE msgID=\"" + messageID + "\"";
        ResultSet resultSet = null;
        resultSet = connector.executeSelect(query);
        return resultSet;
    }

    /**
     * @return Wszystkie ankiety
     */
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

    /**
     * @param messageID ID wiadomości w której jest ankieta
     * @return Wszystkie odpowiedzi dla ankiety która jest w wiadomości o ID messageID
     */
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

    /**
     * @param id ID odpowiedzi z tabeli answers
     * @return wszystkie odpowiedzi użytkowników dla odpowiedzi z tabeli answers o id przekazanej w parametrze
     */
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

    /**
     * Tworzy nową tabelę questionnaire w bazie danych
     */
    private void createTableQuestionnaire() {
        String queryCreate = "CREATE TABLE questionnaire(" +
                "msgID VARCHAR(30) PRIMARY KEY," +
                "channelID VARCHAR(30) NOT NULL," +
                "authorID VARCHAR(30) NOT NULL," +
                "isMultiple BOOLEAN," +
                "isPublic BOOLEAN)";
        createTable(queryCreate);
    }

    /**
     * Tworzy nową tabelę answers w bazie danych
     */
    private void createTableAnswers() {
        String queryCreate = "CREATE TABLE answers(" +
                "id INT(9) UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "msgID VARCHAR(30) NOT NULL," +
                "answer MEDIUMTEXT," +
                "emojiID VARCHAR(30) NOT NULL," +
                "FOREIGN KEY (msgID) REFERENCES questionnaire(msgID))";
        createTable(queryCreate);
    }

    /**
     * Tworzy nową tabelę user_answer w bazie danych
     */
    private void createTableUserAnswer() {
        String queryCreate = "CREATE TABLE user_answer(" +
                "id INT(9) UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "answer_id INT(9) UNSIGNED," +
                "userID VARCHAR(30)," +
                "FOREIGN KEY (answer_id) REFERENCES answers(id))";
        createTable(queryCreate);
    }

    /**
     * Tworzy nową tabelę przekazaną w parametrze
     *
     * @param queryCreate polecenie utworzzenia nowej tabeli
     */
    private void createTable(String queryCreate) {
        connector.executeQuery(queryCreate);
    }

    /**
     * Dodaje do bazy danych nową odpowiedź użytkownika
     *
     * @param idDb   id odpowiedź jaką dął użytkownik
     * @param userID ID użytkownika którego dodajemy
     */
    public void addUserToAnswer(int idDb, String userID) {
        String query = "INSERT INTO user_answer (`answer_id`,`userID`) " +
                "VALUES (\"" + idDb + "\",\"" + userID + "\")";
        connector.executeQuery(query);
    }

    /**
     * Usuwa z bazy danych odpowiedź użytkownika
     *
     * @param idAnswer id odpowiedzi jaką usuwamy użytkownikowi
     * @param userID   uzytkownik którego usuwamy
     */
    public void removeUserAnswer(int idAnswer, String userID) {
        String query = "DELETE FROM user_answer WHERE answer_id=\"" + idAnswer + "\" AND userID=\"" + userID + "\"";
        connector.executeQuery(query);
    }

    /**
     * Usuwa z bazy danych wszystkie odpowiedzi użytkowników, odpowiedzi i samą ankietę
     *
     * @param messageID ID wiadomości w której jest/była ankieta
     */
    public void removeQuestionnaire(String messageID) {
        String removeUserID = "DELETE FROM user_answer WHERE answer_id IN (" +
                "SELECT id FROM answers WHERE msgID=\"" + messageID + "\")";
        String removeAnswers = "DELETE FROM answers WHERE msgID=\"" + messageID + "\"";
        String removeQuestionnaire = "DELETE FROM questionnaire WHERE msgID=\"" + messageID + "\"";
        connector.executeQuery(removeUserID);
        connector.executeQuery(removeAnswers);
        connector.executeQuery(removeQuestionnaire);
    }

    /**
     * Wysyła do bazy danych nową czystą ankietę
     *
     * @param mID ID wiadomości w ktorej jest ankieta
     * @param cID ID kanału na którym jest ankieta
     * @param aID ID autora ankiety
     * @param isM czy ankieta jest wielokrotnego wyboru
     * @param isP czy ankeita jest publiczna
     */
    public void pushNewQuestionnaire(String mID, String cID, String aID, boolean isM, boolean isP) {
        String query = "INSERT INTO questionnaire (`msgID`,`channelID`,`authorID`,`isMultiple`,`isPublic`) " +
                "VALUES (\"%s\",\"%s\",\"%s\",%b,%b)";
        connector.executeQuery(String.format(query, mID, cID, aID, isM, isP));
    }

    /**
     * Wysyła do bazy danych nową czystą odpowiedź
     *
     * @param messageID ID wiadomoścki w której jest ankieta
     * @param answer    odpowiedź z ankiety
     * @param emojiID   ID emoji
     */
    public void pushNewAnswer(String messageID, String answer, String emojiID) {
        String query = "INSERT INTO answers (`msgID`,`answer`,`emojiID`) " +
                "VALUES (\"%s\",\"%s\",\"%s\")";
        connector.executeQuery(String.format(query, messageID, answer, emojiID));
    }
}

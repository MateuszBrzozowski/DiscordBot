package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

class Answer {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private int idDb = -1;
    private String answer;
    private String emojiID;
    private HashSet<String> usersID = new HashSet<>();

    /**
     * @param answer  tekst odpowiedzi z ankiety
     * @param emojiID emoji
     */
    Answer(String answer, String emojiID) {
        this.answer = answer;
        this.emojiID = emojiID;
    }

    String getEmojiID() {
        return emojiID;
    }

    String getAnswer() {
        return answer;
    }

    void addUser(String userID) {
        this.usersID.add(userID);
    }

    void addUser(String userID, String messageID) {
        addUser(userID);
        addUserToDataBase(userID, messageID);
    }

    private void addUserToDataBase(String userID, String messageID) {
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        getIDFromDataBase(messageID, qdb);
        qdb.addUserToAnswer(idDb, userID);
    }

    private void getIDFromDataBase(String messageID, QuestionnaireDatabase qdb) {
        if (idDb == -1) {
            ResultSet resultSet = qdb.getAnswerIDFromDatabase(messageID, emojiID);
            setIdDB(resultSet);
        }
    }

    private void setIdDB(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.next();
                idDb = resultSet.getInt("id");
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public void setIdDb(int idDb) {
        this.idDb = idDb;
    }

    int getCountAnswers() {
        return usersID.size();
    }

    boolean wasUserAnswered(String userID) {
        for (String id : usersID) {
            if (id.equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    void removeUserAnswer(String userID, String messageID) {
        boolean remove = usersID.remove(userID);
        if (remove) {
            removeUserAnswerFromDatabase(userID, messageID);
        }
    }

    void removeUserAnswerFromDatabase(String userID, String messageID) {
        logger.info("Uzytkownik ma odpowiedz i usuwam go z bazy danych");
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        getIDFromDataBase(messageID, qdb);
        qdb.removeUserAnswer(idDb, userID);
    }


}

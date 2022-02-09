package questionnaire;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

class Answer {

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

    boolean addUser(String userID) {
        return this.usersID.add(userID);
    }

    void addUser(String userID, String messageID) {
        boolean isAdded = addUser(userID);
        if (isAdded) {
            addUserToDataBase(userID, messageID);
        }
    }

    private void addUserToDataBase(String userID, String messageID) {
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        getIDFromDataBase(messageID, qdb);
        qdb.addUserToAnswer(idDb, userID);
    }

    private void getIDFromDataBase(String messageID, QuestionnaireDatabase qdb) {
        if (idDb == -1) {
            ResultSet resultSet = qdb.getAnswerIDFromDatabase(messageID);
            setIdDB(resultSet);
        }
    }

    private void setIdDB(ResultSet resultSet) {
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    }
                    int id = resultSet.getInt("id");
                    String emojiIDDB = resultSet.getString("emojiID");
                    if (emojiID.equalsIgnoreCase(emojiIDDB)) {
                        idDb = id;
                    }
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    void setIdDb(int idDb) {
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

    private void removeUserAnswerFromDatabase(String userID, String messageID) {
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        getIDFromDataBase(messageID, qdb);
        qdb.removeUserAnswer(idDb, userID);
    }
}

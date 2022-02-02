package questionnaire;

import java.util.HashSet;

public class Answer {

    private String messageID;
    private String answer;
    private String answerID;
    private HashSet<String> usersID = new HashSet<>();

    Answer(String answer, String answerID) {
        this.answer = answer;
        this.answerID = answerID;
    }

    String getAnswerID() {
        return answerID;
    }

    String getAnswer() {
        return answer;
    }

    void addUser(String userID) {
        this.usersID.add(userID);
    }

    void setMessageID(String messageID) {
        this.messageID = messageID;
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
}

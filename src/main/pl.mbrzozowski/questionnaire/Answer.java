package questionnaire;

import java.util.HashSet;

public class Answer {

    private String messageID;
    private String answer;
    private String answerID;
    private HashSet<String> usersID = new HashSet<>();

    /**
     * @param answer    Tekst odpowiedzi
     * @param messageID ID Ankiety
     */
    public Answer(String answer, String answerID, String messageID) {
        this.messageID = messageID;
        this.answer = answer;
        this.answerID = answerID;
    }

    public Answer(String answer, String answerID) {
        this.answer = answer;
        this.answerID = answerID;
    }

    public String getAnswerID() {
        return answerID;
    }

    public String getAnswer() {
        return answer;
    }

    public void addUser(String userID) {
        this.usersID.add(userID);
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public int getCountAnswers() {
        return usersID.size();
    }

    public boolean wasUserAnswered(String userID) {
        for (String id : usersID) {
            if (id.equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }
}

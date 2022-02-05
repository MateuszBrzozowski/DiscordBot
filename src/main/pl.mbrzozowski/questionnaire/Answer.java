package questionnaire;

import java.util.HashSet;

class Answer {

    private String answer;
    private String answerID;
    private HashSet<String> usersID = new HashSet<>();

    /**
     * @param answer   tekst odpowiedzi z ankiety
     * @param answerID emoji
     */
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

    void removeUserAnswer(String userID) {
        usersID.remove(userID);
    }
}

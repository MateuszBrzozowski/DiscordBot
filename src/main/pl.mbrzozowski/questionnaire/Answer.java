package questionnaire;

import java.util.ArrayList;
import java.util.List;

public class Answer {

    private String messageID;
    private String answer;
    private List<String> usersID = new ArrayList<>();

    /**
     * @param answer    Tekst odpowiedzi
     * @param messageID ID Ankiety
     */
    public Answer(String answer, String messageID) {
        this.messageID = messageID;
        this.answer = answer;
    }
}

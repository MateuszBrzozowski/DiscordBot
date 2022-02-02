package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Questionnaire {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String messageID;
    private String authorID;
    private String channelID;
    private String question;
    private List<Answer> answers = new ArrayList<>();

    Questionnaire(QuestionnaireBuilder questionnaireBuilder) {
        this.messageID = questionnaireBuilder.getMessageID();
        this.authorID = questionnaireBuilder.getAuthorID();
        this.channelID = questionnaireBuilder.getChannelID();
        this.question = questionnaireBuilder.getQuestion();
        createAnsewrs(questionnaireBuilder.getAnswers());
    }

    private void createAnsewrs(List<Answer> answers) {
        this.answers = answers;
        for (int i = 0; i < answers.size(); i++) {
            this.answers.get(i).setMessageID(messageID);
        }
    }

    void addAnswer(String emoji, String userID) {
        if (wasUserAnswered(userID)) {
            removeUserAnswer(userID);
        }
        for (Answer a : answers) {
            if (a.getAnswerID().equalsIgnoreCase(emoji)) {
                a.addUser(userID);
            }
        }
        updateEmbed();

    }

    private void removeUserAnswer(String userID) {
        for (Answer a : answers){
            a.removeUserAnswer(userID);
        }
    }

    private boolean wasUserAnswered(String userID) {
        for (Answer a : answers) {
            if (a.wasUserAnswered(userID)) {
                return true;
            }
        }
        return false;
    }

    private void updateEmbed() {
        QuestionnaireBuilder builder = new QuestionnaireBuilder(this);
        builder.updateEmbed();
    }

    String getMessageID() {
        return messageID;
    }

    String getChannelID() {
        return channelID;
    }

    List<Answer> getAnswers() {
        return answers;
    }
}

package questionnaire;

import helpers.RoleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Questionnaire {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String messageID;
    private String authorID = RoleID.DEV_ID;
    private String channelID = null;
    private String question = null;
    private List<Answer> answers = new ArrayList<>();

    public Questionnaire(QuestionnaireBuilder questionnaireBuilder) {
        this.messageID = questionnaireBuilder.getMessageID();
        this.authorID = questionnaireBuilder.getAuthorID();
        this.channelID = questionnaireBuilder.getChannelID();
        this.question = questionnaireBuilder.getQuestion();
        createAnsewrs(questionnaireBuilder.getAnswers());
        logger.info(messageID);
        logger.info(channelID);
        logger.info(String.valueOf(answers.size()));
    }

    private void createAnsewrs(List<String> answers) {
        for (String s : answers) {
            Answer answer = new Answer(s, messageID);
            this.answers.add(answer);
        }
    }

    public Questionnaire addAnswer(String answerText) {
        Answer answer = new Answer(answerText, messageID);
        this.answers.add(answer);
        return this;
    }
}

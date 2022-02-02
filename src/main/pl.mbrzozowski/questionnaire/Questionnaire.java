package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Questionnaire {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public Questionnaire(String contentDisplay, String userID, String channelID) {

        contentDisplay = contentDisplay.substring(9); //Commands.QUESTIONNAIRE.length() !ankieta =  9

        String[] questionAndAnswer = contentDisplay.split("\\|");

        logger.info(String.valueOf(questionAndAnswer.length));

        QuestionnaireBuilder builder = new QuestionnaireBuilder();
        builder.setAuthorID(userID)
                .setQuestion(questionAndAnswer[0])
                .setChannelID(channelID);

        if (questionAndAnswer.length >= 3) {
            for (int i = 1; i < questionAndAnswer.length; i++) {
                builder.addAnswer(questionAndAnswer[i]);
            }
        }
        builder.build();
    }
}

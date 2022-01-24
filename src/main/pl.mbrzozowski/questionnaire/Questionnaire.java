package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Questionnaire {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public Questionnaire(String contentDisplay, String userID, String channelID, boolean isPublic) {

        if (isPublic) {
            contentDisplay = contentDisplay.substring(14); //Commands.QUESTIONNAIRE_PUBLIC.length() !ankietaJawna =  14
        } else {
            contentDisplay = contentDisplay.substring(9); //Commands.QUESTIONNAIRE.length() !ankieta =  9
        }
        String[] questionAndAnswer = contentDisplay.split("\\|");


        QuestionnaireBuilder builder = new QuestionnaireBuilder();
        builder.setAuthorID(userID)
                .isPublic(isPublic)
                .setQuestion(questionAndAnswer[0])
                .setChannelID(channelID);

        if (questionAndAnswer.length >= 3) {
            for (int i = 1; i < 5; i++) {
                builder.addAnswer(questionAndAnswer[i]);
            }
        }
        logger.info(String.valueOf(questionAndAnswer.length));
        builder.build();
    }
}

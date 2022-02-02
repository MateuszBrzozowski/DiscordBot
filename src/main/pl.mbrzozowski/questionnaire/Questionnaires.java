package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Questionnaires {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private List<Questionnaire> questionnaires = new ArrayList<>();

    public static void buildQuestionaire(String contentDisplay, String userID, String channelID) {
        contentDisplay = contentDisplay.substring(9); //Commands.QUESTIONNAIRE.length() !ankieta =  9

        String[] questionAndAnswer = contentDisplay.split("\\|");

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

    public void addQuestionnaire(QuestionnaireBuilder questionnaireBuilder) {
        Questionnaire questionnaire = new Questionnaire(questionnaireBuilder);
        questionnaires.add(questionnaire);
    }

    public void saveAnswer(String emoji, String messageId, String userID) {
        questionnaires.get(getIndex(messageId)).addAnswer(emoji, userID);

    }

    private int getIndex(String messageId) {
        logger.info("szukam indexu");
        for (int i = 0; i < questionnaires.size(); i++) {
            if (questionnaires.get(i).getMessageID().equalsIgnoreCase(messageId)) {
                logger.info(String.valueOf(i));
                return i;
            }
        }
        return -1;
    }
}

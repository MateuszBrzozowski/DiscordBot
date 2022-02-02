package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Questionnaires {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private List<Questionnaire> questionnaires = new ArrayList<>();

    /**
     * Tworzy ankiętę na podstawię polecenia wpisanego na kanale
     *
     * @param contentDisplay polecenie tworzenia ankiety - MIN_ODP 0, MAX_ODP 9
     *                       - (przykład. !ankieta PYTANIE | ODP 1 | ODP 2 | ODP 3 / !ankieta PYTANIE)
     * @param userID         ID użytkownika który tworzy ankiete
     * @param channelID      ID kanału na którym tworzona jest ankieta
     */
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

    void addQuestionnaire(QuestionnaireBuilder questionnaireBuilder) {
        Questionnaire questionnaire = new Questionnaire(questionnaireBuilder);
        questionnaires.add(questionnaire);
    }

    /**
     * @param emoji     Emoji które zostało kliknięte przez użytkownika
     * @param messageId ID wiadomości dla której została dodana reakcja
     * @param userID    ID użytkownika który dał reakcję
     */
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

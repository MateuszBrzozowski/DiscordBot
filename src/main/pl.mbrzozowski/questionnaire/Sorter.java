package questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Sorter {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public List<Answer> sortQuestionnaireAnswersList(List<Answer> answers) {
        ArrayList<Answer> answersSorted = new ArrayList<>();
        answersSorted.add(answers.get(0));
        int whereInsert;
        for (int i = 1; i < answers.size(); i++) {
            whereInsert = i;
            for (int j = i - 1; j >= 0; j--) {
                if (answers.get(i).getCountAnswers() > answersSorted.get(j).getCountAnswers()) {
                    logger.info(String.valueOf(j));
                    whereInsert = j;
                }
            }
            answersSorted.add(whereInsert, answers.get(i));
        }
        return answersSorted;
    }

}

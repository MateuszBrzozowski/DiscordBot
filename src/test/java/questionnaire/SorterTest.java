package questionnaire;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SorterTest {

    @Test
    public void sortQuestionnaireAnswersList() {
        Answer answer1 = new Answer("odp1 (2)","2");
        Answer answer2 = new Answer("odp2 (1)","1");
        Answer answer3 = new Answer("odp3 (2)","3");

        answer1.addUser("1");
        answer1.addUser("2");
        answer2.addUser("3");
        answer3.addUser("4");
        answer3.addUser("5");
        answer3.addUser("6");

        List<Answer> answers = new ArrayList<>();
        answers.add(answer1);
        answers.add(answer2);
        answers.add(answer3);

        Sorter sorter = new Sorter();
        answers = sorter.sortQuestionnaireAnswersList(answers);

        Assert.assertEquals(3,answers.get(0).getCountAnswers());
        Assert.assertEquals(2,answers.get(1).getCountAnswers());
        Assert.assertEquals(1,answers.get(2).getCountAnswers());
    }

}
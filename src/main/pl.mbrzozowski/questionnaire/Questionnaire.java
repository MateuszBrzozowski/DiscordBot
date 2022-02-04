package questionnaire;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
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
    private boolean isMultiple;
    private boolean isPublic;
    private List<Answer> answers = new ArrayList<>();

    Questionnaire(QuestionnaireBuilder questionnaireBuilder) {
        this.messageID = questionnaireBuilder.getMessageID();
        this.authorID = questionnaireBuilder.getAuthorID();
        this.channelID = questionnaireBuilder.getChannelID();
        this.question = questionnaireBuilder.getQuestion();
        this.isMultiple = questionnaireBuilder.isMultiple();
        this.isPublic = questionnaireBuilder.isPublic();
        createAnsewrs(questionnaireBuilder.getAnswers());
    }

    private void createAnsewrs(List<Answer> answers) {
        this.answers = answers;
        for (int i = 0; i < answers.size(); i++) {
            this.answers.get(i).setMessageID(messageID);
        }
    }

    void addAnswer(String emoji, String userID) {
        if (!isMultiple) {
            if (wasUserAnswered(userID)) {
                removeUserAnswer(userID);
            }
        }
        for (Answer a : answers) {
            if (a.getAnswerID().equalsIgnoreCase(emoji)) {
                a.addUser(userID);
            }
        }
        updateEmbed();
    }

    private void removeUserAnswer(String userID) {
        for (Answer a : answers) {
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

    void endedEmbed() {
        QuestionnaireBuilder builder = new QuestionnaireBuilder(this);
        builder.ended();
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

    String getAuthorID() {
        return authorID;
    }

    boolean isPublic() {
        return isPublic;
    }

    boolean isMultiple() {
        return isMultiple;
    }

    /**
     * Usuwa w wiadomości message wszystkie reakcje usera oprócz emoji przekazanej w parametrze
     *
     * @param message wiadomośc w której usuwane są reakcje
     * @param emoji   emoji które ma nie zostać usuniete
     * @param user    którego reakcje są usuwane
     */
    void remoweReaction(Message message, String emoji, User user) {
        for (Answer a : answers) {
            if (a.wasUserAnswered(user.getId())) {
                logger.info("User odpowiedział " + a.getAnswer());
                if (!emoji.equalsIgnoreCase(a.getAnswerID())) {
                    a.removeUserAnswer(user.getId());
                    message.removeReaction(a.getAnswerID(), user).queue();
                }
            }
        }
    }


}

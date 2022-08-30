package ranger.questionnaire;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class Questionnaire {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected String messageID;
    protected String authorID;
    protected String channelID = null;
    protected String question = null;
    protected boolean isMultiple = false;
    protected boolean isPublic = false;
    protected List<Answer> answers = new ArrayList<>();

    protected Questionnaire() {
    }

    Questionnaire(QuestionnaireBuilder questionnaireBuilder) {
        this.messageID = questionnaireBuilder.getMessageID();
        this.authorID = questionnaireBuilder.getAuthorID();
        this.channelID = questionnaireBuilder.getChannelID();
        this.question = questionnaireBuilder.getQuestion();
        this.isMultiple = questionnaireBuilder.isMultiple();
        this.isPublic = questionnaireBuilder.isPublic();
        this.answers = questionnaireBuilder.getAnswers();
    }

    void addAnswer(String emoji, String userID) {
        if (!isMultiple) {
            if (wasUserAnswered(userID)) {
                removeUserAnswers(userID);
            }
        }
        for (Answer a : answers) {
            if (a.getEmojiID().equalsIgnoreCase(emoji)) {
                a.addUser(userID, messageID);
            }
        }
        updateEmbed();
    }

    void removeAnswer(String emoji, String userId) {
        for (Answer a : answers) {
            if (a.getEmojiID().equalsIgnoreCase(emoji)) {
                a.removeUserAnswer(userId, messageID);
            }
        }
        updateEmbed();
    }

    private void removeUserAnswers(String userID) {
        for (Answer a : answers) {
            a.removeUserAnswer(userID, messageID);
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

    String getQuestion() {
        return question;
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
                if (!emoji.equalsIgnoreCase(a.getEmojiID())) {
                    a.removeUserAnswer(user.getId(), messageID);
                    message.removeReaction(a.getEmojiID(), user).queue();
                }
            }
        }
    }

    public boolean isCorrectReaction(String emoji) {
        for (Answer a : answers) {
            if (a.getEmojiID().equalsIgnoreCase(emoji)) {
                return true;
            }
        }
        return false;
    }
}

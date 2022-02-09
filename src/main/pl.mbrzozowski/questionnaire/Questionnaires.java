package questionnaire;

import database.DBConnector;
import helpers.Commands;
import helpers.RoleID;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Questionnaires {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private List<Questionnaire> questionnaires = new ArrayList<>();
    private static int questionAndAnswerCount;

    /**
     * Tworzy ankiętę na podstawię polecenia wpisanego na kanale
     *
     * @param contentDisplay polecenie tworzenia ankiety - MIN_ODP 0, MAX_ODP 9
     * @param userID         ID użytkownika który tworzy ankiete
     * @param channelID      ID kanału na którym tworzona jest ankieta
     */
    public static void buildQuestionaire(String contentDisplay, String userID, String channelID) {
        contentDisplay = contentDisplay.substring(Commands.QUESTIONNAIRE.length());
        QuestionnaireBuilder builder = getBuilder(contentDisplay, userID, channelID);
        builder.build();
    }

    /**
     * Tworzy ankiętę wielokrotnego wyboru na podstawię polecenia wpisanego na kanale
     *
     * @param contentDisplay polecenie tworzenia ankiety - MIN_ODP 0, MAX_ODP 9
     * @param userID         ID użytkownika który tworzy ankiete
     * @param channelID      ID kanału na którym tworzona jest ankieta
     */
    public static void buildQuestionaireMultiple(String contentDisplay, String userID, String channelID) {
        contentDisplay = contentDisplay.substring(Commands.QUESTIONNAIRE_MULTIPLE.length());
        QuestionnaireBuilder builder = getBuilder(contentDisplay, userID, channelID);
        if (questionAndAnswerCount >= 3) {
            builder.asMultiple();
        }
        builder.build();
    }

    /**
     * Tworzy ankiętę publiczną na podstawię polecenia wpisanego na kanale
     *
     * @param contentDisplay polecenie tworzenia ankiety - MIN_ODP 0, MAX_ODP 9
     * @param userID         ID użytkownika który tworzy ankiete
     * @param channelID      ID kanału na którym tworzona jest ankieta
     */
    public static void buildQuestionairePublic(String contentDisplay, String userID, String channelID) {
        contentDisplay = contentDisplay.substring(Commands.QUESTIONNAIRE_PUBLIC.length());
        QuestionnaireBuilder builder = getBuilder(contentDisplay, userID, channelID);
        builder.asPublic();
        builder.build();
    }

    /**
     * Tworzy ankiętę publiczną wielokrotnego wyboru na podstawię polecenia wpisanego na kanale
     *
     * @param contentDisplay polecenie tworzenia ankiety - MIN_ODP 0, MAX_ODP 9
     * @param userID         ID użytkownika który tworzy ankiete
     * @param channelID      ID kanału na którym tworzona jest ankieta
     */
    public static void buildQuestionairePublicMultiple(String contentDisplay, String userID, String channelID) {
        contentDisplay = contentDisplay.substring(Commands.QUESTIONNAIRE_PUBLIC_MULTIPLE.length());
        QuestionnaireBuilder builder = getBuilder(contentDisplay, userID, channelID);
        builder.asPublic();
        if (questionAndAnswerCount >= 3) {
            builder.asMultiple();
        }
        builder.build();
    }

    /**
     * Dodaje Ankiete do listy
     *
     * @param questionnaireBuilder builder ankiety
     */
    void addQuestionnaire(QuestionnaireBuilder questionnaireBuilder) {
        Questionnaire questionnaire = new Questionnaire(questionnaireBuilder);
        questionnaires.add(questionnaire);
    }

    /**
     * Tworzy builder ankiety na podstawie przekazanego polcenia
     *
     * @param contentDisplay polecenie tworzenia ankiety
     * @param userID         ID użytkownika który tworzy ankiete
     * @param channelID      kanał na którym tworzona jest ankieta
     * @return
     */
    @NotNull
    private static QuestionnaireBuilder getBuilder(String contentDisplay, String userID, String channelID) {
        String[] questionAndAnswer = contentDisplay.split("\\|");
        questionAndAnswerCount = questionAndAnswer.length;
        QuestionnaireBuilder builder = new QuestionnaireBuilder();
        builder.setAuthorID(userID)
                .setQuestion(questionAndAnswer[0])
                .setChannelID(channelID);

        if (questionAndAnswer.length >= 3) {
            for (int i = 1; i < questionAndAnswer.length; i++) {
                builder.addAnswer(questionAndAnswer[i]);
            }
        }
        return builder;
    }

    /**
     * @param emoji     Emoji które zostało kliknięte przez użytkownika
     * @param messageId ID wiadomości dla której została dodana reakcja
     * @param userID    ID użytkownika który dał reakcję
     */
    public void saveAnswer(String emoji, String messageId, String userID) {
        questionnaires.get(getIndex(messageId)).addAnswer(emoji, userID);
    }

    public void removeAnswer(String emoji, String messageId, String userId) {
        questionnaires.get(getIndex(messageId)).removeAnswer(emoji,userId);
    }

    /**
     * Zwraca index z listy ankiety przekazanej w parametrze
     *
     * @param messageId ID wiadomości w której jest ankieta
     * @return
     */
    private int getIndex(String messageId) {
        for (int i = 0; i < questionnaires.size(); i++) {
            if (questionnaires.get(i).getMessageID() != null) {
                if (questionnaires.get(i).getMessageID().equalsIgnoreCase(messageId)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Sprawdza czy wskazana ankieta jest publiczna
     *
     * @param index index ankiety którą sprawdzamy
     * @return true - jeśli ankieta jest publiczna, w innym przypadku false
     */
    public boolean isPublic(int index) {
        return questionnaires.get(index).isPublic();
    }

    /**
     * Srawdza czy ankieta jest wielokrotnego wyboru
     *
     * @param index ankiety którą sprawdzamy
     * @return true - jesli ankieta jest wielokrotnego wyboru, w innym przypadku false
     */
    public boolean isMultiple(int index) {
        return questionnaires.get(index).isMultiple();
    }

    /**
     * Kończy ankietę. Usuwa przyciski i reakcje w wiadomości/ankiecie jeżeli uzytkownik o ID przekazanym w parametrze
     * jest autorem ankiety.
     *
     * @param messageID ID wiadomości w której jest ankieta
     * @param channelID ID kanału na którym znajduje się ankieta
     * @param userID    ID użytkowwnika który próbuje zakończyć ankietę
     */
    public void end(String messageID, String channelID, String userID) {
        if (isAuthor(messageID, userID)) {
            removeReactionsAndButtons(messageID, channelID);
            questionnaires.get(getIndex(messageID)).endedEmbed();
            removeQuestionnaire(messageID);
        }
    }

    public void removeQuestionnaire(String messageID) {
        removeQuestionnaireFromDataBase(messageID);
        questionnaires.remove(getIndex(messageID));
    }

    /**
     * Usuwa wszystkie dane z bazy, ankiety o id messageID
     *
     * @param messageID ID ankiety
     */
    private void removeQuestionnaireFromDataBase(String messageID) {
        String removeUserID = "DELETE FROM user_answer WHERE answer_id IN (" +
                "SELECT id FROM answers WHERE msgID=\"" + messageID + "\")";
        String removeAnswers = "DELETE FROM answers WHERE msgID=\"" + messageID + "\"";
        String removeQuestionnaire = "DELETE FROM questionnaire WHERE msgID=\"" + messageID + "\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(removeUserID);
        connector.executeQuery(removeAnswers);
        connector.executeQuery(removeQuestionnaire);
    }

    /**
     * Usuwa reakcje i przycisk na ankiecie
     *
     * @param messageID ID wiadomości w której jest ankieta
     * @param channelID ID kanału na którym jest ankieta
     */
    private void removeReactionsAndButtons(String messageID, String channelID) {
        JDA jda = Repository.getJda();
        jda.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
            Button b = Button.primary("null", "Ankieta zakończona.");
            b = b.asDisabled();
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            message.editMessage(messageEmbed).setActionRow(b).queue();
            message.clearReactions().queue();
        });
    }

    /**
     * @param messageID ID wiadomości w której jest ankieta
     * @param userID    ID użytkownika którego sprawdzamy
     * @return true - jeśli użytkownik to autor ankiety, w innym przypadku false
     */
    private boolean isAuthor(String messageID, String userID) {
        if (questionnaires.get(getIndex(messageID)).getAuthorID().equalsIgnoreCase(userID)) {
            return true;
        } else return RoleID.DEV_ID.equalsIgnoreCase(userID);
    }

    /**
     * Usuwa w wiadomości message wszystkie reakcje usera oprócz emoji przekazanej w parametrze
     *
     * @param message wiadomośc w której usuwane są reakcje
     * @param emoji   emoji które ma nie zostać usuniete
     * @param user    którego reakcje są usuwane
     */
    public void removeReaction(Message message, String emoji, User user) {
        Questionnaire questionnaire = questionnaires.get(getIndex(message.getId()));
        questionnaire.remoweReaction(message, emoji, user);
    }

    /**
     * Pobiera z bazy danych wszystkie ankiety wraz z odpowiedziami i dodaje na liste.
     */
    public void initialize() {
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        ResultSet allQuestionnaire = qdb.getAllQuestionnaire();
        QuestionnaireBuilder builder = new QuestionnaireBuilder();
        pullAllQuestionnairesFromDataBase(allQuestionnaire, builder, qdb);
        addQuestionnaire(builder);
    }

    private void pullAllAnswersFromDataBase(ResultSet resultSet, QuestionnaireBuilder builder, QuestionnaireDatabase qdb) {
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    }
                    int id = resultSet.getInt("id");
                    String answerText = resultSet.getString("answer");
                    String answerID = resultSet.getString("emojiID");
                    Answer answer = new Answer(answerText, answerID);
                    answer.setIdDb(id);
                    addAllUsersAnswersFromDatabase(qdb.getAllUserAnswerWithID(id), answer);
                    builder.addAnswer(answer);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    private void addAllUsersAnswersFromDatabase(ResultSet resultSet, Answer answer) {
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    }
                    String userID = resultSet.getString("userID");
                    answer.addUser(userID);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    private void pullAllQuestionnairesFromDataBase(ResultSet resultSet, QuestionnaireBuilder builder, QuestionnaireDatabase qdb) {
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    }
                    String messageID = resultSet.getString("msgID");
                    String channelID = resultSet.getString("channelID");
                    String authorID = resultSet.getString("authorID");
                    boolean isMultiple = resultSet.getBoolean("isMultiple");
                    boolean isPublic = resultSet.getBoolean("isPublic");
                    builder.setMessageID(messageID)
                            .setAuthorID(authorID)
                            .setChannelID(channelID);
                    if (isMultiple) {
                        builder.asMultiple();
                    }
                    if (isPublic) {
                        builder.asPublic();
                    }
                    pullAllAnswersFromDataBase(qdb.getAllAnswersFromQuestionnaireID(messageID), builder, qdb);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    public int getQuestionnaireIndex(String messageId) {
        for (int i = 0; i < questionnaires.size(); i++) {
            if (questionnaires.get(i).getMessageID() != null) {
                if (questionnaires.get(i).getMessageID().equalsIgnoreCase(messageId)) {
                    return i;
                }
            }
        }
        return -1;
    }
}

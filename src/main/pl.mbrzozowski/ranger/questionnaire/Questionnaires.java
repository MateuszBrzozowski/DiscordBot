package ranger.questionnaire;

import ranger.helpers.Commands;
import ranger.helpers.RoleID;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
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

    /**
     * @param emoji     Emoji które zostało usunięte przez użytkownika
     * @param messageId ID wiadomości w której emoji zostało usunięte
     * @param userId    ID użytkownika który usunął emoji
     */
    public void removeAnswer(String emoji, String messageId, String userId) {
        questionnaires.get(getIndex(messageId)).removeAnswer(emoji, userId);
    }

    /**
     * Zwraca index z listy ankiety przekazanej w parametrze
     *
     * @param messageId ID wiadomości w której jest ankieta
     * @return
     */
    public int getIndex(String messageId) {
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

    /**
     * Usuwa ankiętę z listy i z bazy danych
     *
     * @param messageID ID wiadomości w której jest ankieta
     */
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
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        qdb.removeQuestionnaire(messageID);
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
            message.editMessageEmbeds(messageEmbed).setActionRow(b).queue();
            message.clearReactions().queue();
        });
    }

    /**
     * @param messageID ID wiadomości w której jest ankieta
     * @param userID    ID użytkownika którego sprawdzamy
     * @return true - jeśli użytkownik to autor ankiety lub developer, w innym przypadku false
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
        checkQuestionnnaires();
    }

    /**
     * Ustawia pobrane odpowiedzi do odpowiedniej ankiety
     *
     * @param resultSet Wszystkie odpowiedzi z bazy danych pasujace do ankiety.
     * @param builder   ankiety do której chcemy przypisać odpowiedzi
     * @param qdb       połączenie z bazą danych
     */
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
                    pullAllUsersAnswersFromDatabase(qdb.getAllUserAnswerWithID(id), answer);
                    builder.addAnswer(answer);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    /**
     * Ustawia pobrane odpowiedzi użytkowników do odpowiedniej odpowiedzi/
     *
     * @param resultSet wszyscy użytkownicy danej odpowiedzi
     * @param answer    odpowiedź do której są dodawani użytkownicy
     */
    private void pullAllUsersAnswersFromDatabase(ResultSet resultSet, Answer answer) {
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

    /**
     * Pobrane z bazy danych informację dodaje na listę ankiet
     *
     * @param resultSet pobrane z bazy danych wszystkie ankiety
     * @param builder   ankieta tworzona na podstawie danych z baz danych
     * @param qdb       połączenie z bazami danych
     */
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
                    questionnaires.add(builder);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    /**
     * Sprawdza wszystkie ankiety czy istnieją
     */
    private void checkQuestionnnaires() {
        for (Questionnaire q : questionnaires) {
            checkQuestionnnaire(q.getChannelID(), q.getMessageID());
        }
    }

    /**
     * Sprawdza poszczególną ankiętę czy istnieje. Jeżeli nie istnieje usuwa ją z listy i z bazy danych
     *
     * @param channelID ID kanału na którym suzkamy ankiety
     * @param messageID ID wiadomości w której szukamy ankiety
     */
    private void checkQuestionnnaire(String channelID, String messageID) {
        JDA jda = Repository.getJda();
        jda.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
            logger.info("jest taka wiadomosc, bez tej lini to nie zadziała.");
        }, (failure) -> {
            if (failure instanceof ErrorResponseException) {
                ErrorResponseException ex = (ErrorResponseException) failure;
                if (ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                    removeQuestionnaire(messageID);
                }
            }
        });
    }

    /**
     * Sprawdza czy rekacja przekazana w parametrze jest właściwą dla danej ankiety
     *
     * @param index ankiety
     * @param emoji które sprawdzamy czy jest prawidłowe
     * @return true - jeżeli emoji istnieje jako odpowiedź do ankiety, w innym przypadku false
     */
    public boolean isCorrectReaction(int index, String emoji) {
        return questionnaires.get(index).isCorrectReaction(emoji);
    }
}

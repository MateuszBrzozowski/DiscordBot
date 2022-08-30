package ranger.questionnaire;

import ranger.embed.EmbedSettings;
import ranger.helpers.ComponentId;
import ranger.helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class QuestionnaireBuilder extends Questionnaire {

    private final String COUNT_ANSWERS = "Razem głosów - ";
    private final String YES = "TAK";
    private final String NO = "NIE";
    private final String QUESTIONNAIRE = "Ankieta";
    private final String QUESTION = "Pytanie";
    private final String CREATED_BY = "Utworzono przez ";
    private final String ANSWERS = "Odpowiedzi";
    private final String END = "Zakończ";

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private boolean isEnding = false;

    QuestionnaireBuilder() {
    }

    QuestionnaireBuilder(Questionnaire questionnaire) {
        this.channelID = questionnaire.getChannelID();
        this.messageID = questionnaire.getMessageID();
        this.answers = questionnaire.getAnswers();
    }


    QuestionnaireBuilder setQuestion(String question) {
        this.question = question;
        return this;
    }

    QuestionnaireBuilder setMessageID(String messageID) {
        this.messageID = messageID;
        return this;
    }

    QuestionnaireBuilder setChannelID(String channelID) {
        this.channelID = channelID;
        return this;
    }

    QuestionnaireBuilder addAnswer(String answer) {
        if (answers.size() < 9) {
            Answer a = new Answer(answer, getEmoji(answers.size()));
            answers.add(a);
        }
        return this;
    }

    QuestionnaireBuilder addAnswer(Answer answer) {
        answers.add(answer);
        return this;
    }

    QuestionnaireBuilder setAuthorID(String userID) {
        this.authorID = userID;
        return this;
    }

    QuestionnaireBuilder asMultiple() {
        this.isMultiple = true;
        return this;
    }

    QuestionnaireBuilder asPublic() {
        this.isPublic = true;
        return this;
    }

    void build() {
        if (channelID != null && question != null) {
            addAnswerYesAndNo();
            sendInterfaceToTextChannel(getEmbedBuilder());
        }
    }

    /**
     * Wysyła Embed na kanał tekstowy, ustawia ID wiadomości którą jest ankieta i dodaje do repozytorium Ankiete
     *
     * @param builder Embed - wygląd ankiety
     */
    private void sendInterfaceToTextChannel(EmbedBuilder builder) {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelID);
        textChannel.sendMessageEmbeds(builder.build()).queue(message -> {
            MessageEmbed mOld = message.getEmbeds().get(0);
            String msgID = message.getId();
            message.editMessageEmbeds(mOld).setActionRow(
                    Button.danger(ComponentId.QUESTIONNAIRE_END + msgID, END)
            ).queue();
            this.setMessageID(msgID);
            pushQuestionnaireToDataBase();
            Repository.getQuestionnaires().addQuestionnaire(this);
            addReactions(message);
        });
    }

    private void pushQuestionnaireToDataBase() {
        QuestionnaireDatabase qdb = new QuestionnaireDatabase();
        qdb.pushNewQuestionnaire(messageID, channelID, authorID, isMultiple, isPublic);
        for (Answer a : answers) {
            qdb.pushNewAnswer(messageID, a.getAnswer(), a.getEmojiID());
        }
    }

    /**
     * Dodaje odpowiedzi TAK/NIE jeżeli żadne odpowiedzi nie zostały ustawione
     */
    private void addAnswerYesAndNo() {
        if (answers.isEmpty()) {
            Answer answerYes = new Answer(YES, QuestionnaireStaticHelpers.EMOJI_YES);
            Answer answerNo = new Answer(NO, QuestionnaireStaticHelpers.EMOJI_NO);
            this.answers.add(answerYes);
            this.answers.add(answerNo);
        }
    }

    /**
     * Ustawia wygląd ankiety
     *
     * @return zwraca embedbuilder z ustawionymi parametrami
     */
    @NotNull
    private EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(QUESTIONNAIRE);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        Color questionnaireColor = new Color(59, 136, 195);
        builder.setColor(questionnaireColor);
        builder.addField(QUESTION, question, false);
        builder.setFooter(CREATED_BY + Users.getUserNicknameFromID(authorID));
        builder.addField(ANSWERS, getAnswersField(), false);
        int allCountAnswers = getAllCountAnserws();
        builder.addField("", COUNT_ANSWERS + allCountAnswers, false);
        return builder;
    }

    private int getAllCountAnserws() {
        int allCountAnserws = 0;
        for (Answer a : answers) {
            allCountAnserws += a.getCountAnswers();
        }
        return allCountAnserws;
    }

    private void addReactions(Message message) {
        switch (answers.size()) {
            case 2:
                if (answers.get(0).getEmojiID().equalsIgnoreCase(QuestionnaireStaticHelpers.EMOJI_YES)) {
                    message.addReaction(QuestionnaireStaticHelpers.EMOJI_YES).queue();
                    message.addReaction(QuestionnaireStaticHelpers.EMOJI_NO).queue();
                } else {
                    addReactionExactCount(message, 2);
                }
                break;
            case 3:
                addReactionExactCount(message, 3);
                break;
            case 4:
                addReactionExactCount(message, 4);
                break;
            case 5:
                addReactionExactCount(message, 5);
                break;
            case 6:
                addReactionExactCount(message, 6);
                break;
            case 7:
                addReactionExactCount(message, 7);
                break;
            case 8:
                addReactionExactCount(message, 8);
                break;
            case 9:
                addReactionExactCount(message, 9);
                break;
            default:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_YES).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_NO).queue();
        }
    }

    /**
     * Dodaje konkretną liczbę reakcji do wiadomości
     *
     * @param message wiadomośc do której mają zostać dodane reakcje
     * @param count   liczba ile reakcji ma zostać dodanych
     */
    private void addReactionExactCount(Message message, int count) {
        List<String> emojis = getEmojisList();
        for (int i = 0; i < count; i++) {
            message.addReaction(emojis.get(i)).queue();
        }
    }

    @NotNull
    private List<String> getEmojisList() {
        List<String> emojis = new ArrayList<>();
        emojis.add(QuestionnaireStaticHelpers.EMOJI_A);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_B);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_C);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_D);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_E);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_F);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_G);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_H);
        emojis.add(QuestionnaireStaticHelpers.EMOJI_I);
        return emojis;
    }

    private String getAnswersField() {
        String result = "";
        for (int i = 0; i < answers.size(); i++) {
            if (!answers.get(0).getEmojiID().equalsIgnoreCase(QuestionnaireStaticHelpers.EMOJI_YES)) {
                if (!isEnding) {
                    result += getEmoji(i);
                }
            }
            result += " " + answers.get(i).getAnswer() + " **- " + answers.get(i).getCountAnswers() + " Głosów ";
            if (isEnding) {
                result += "(" + getPercent(answers.get(i)) + "%)";
            }
            result += "**\n";
        }
        return result;
    }

    private String getPercent(Answer answer) {
        float countAnswersFloat = answer.getCountAnswers();
        float countAllAnswersFloat = getAllCountAnserws();
        return String.valueOf(Math.round(countAnswersFloat / countAllAnswersFloat * 100));
    }

    private String getAnsweredFieldEnd() {
        if (answers.size() > 2) {
            sortAnswers();
        }
        return getAnswersField();
    }


    private void sortAnswers() {
        Sorter sorter = new Sorter();
        answers = sorter.sortQuestionnaireAnswersList(answers);
    }

    private String getEmoji(int i) {
        List<String> emojis = getEmojisList();
        return emojis.get(i);
    }

    void updateEmbed() {
        JDA jda = Repository.getJda();
        jda.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
            MessageEmbed mOld = message.getEmbeds().get(0);
            List<MessageEmbed.Field> fieldsOld = mOld.getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();

            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i == 1) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(ANSWERS, getAnswersField(), false);
                    fieldsNew.add(fieldNew);
                } else if (i == 2) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field("", COUNT_ANSWERS + getAllCountAnserws(), false);
                    fieldsNew.add(fieldNew);
                } else {
                    fieldsNew.add(fieldsOld.get(i));
                }
            }

            MessageEmbed m = new MessageEmbed(mOld.getUrl()
                    , mOld.getTitle()
                    , mOld.getDescription()
                    , mOld.getType()
                    , mOld.getTimestamp()
                    , mOld.getColorRaw()
                    , mOld.getThumbnail()
                    , mOld.getSiteProvider()
                    , mOld.getAuthor()
                    , mOld.getVideoInfo()
                    , mOld.getFooter()
                    , mOld.getImage()
                    , fieldsNew);
            message.editMessageEmbeds(m).queue();
        });
    }

    void ended() {
        isEnding = true;
        JDA jda = Repository.getJda();
        jda.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
            MessageEmbed mOld = message.getEmbeds().get(0);
            List<MessageEmbed.Field> fieldsOld = mOld.getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();

            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i == 1) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(ANSWERS, getAnsweredFieldEnd(), false);
                    fieldsNew.add(fieldNew);
                } else {
                    fieldsNew.add(fieldsOld.get(i));
                }
            }

            MessageEmbed m = new MessageEmbed(mOld.getUrl()
                    , mOld.getTitle()
                    , mOld.getDescription()
                    , mOld.getType()
                    , mOld.getTimestamp()
                    , mOld.getColorRaw()
                    , mOld.getThumbnail()
                    , mOld.getSiteProvider()
                    , mOld.getAuthor()
                    , mOld.getVideoInfo()
                    , mOld.getFooter()
                    , mOld.getImage()
                    , fieldsNew);
            message.editMessageEmbeds(m).queue();
        });
    }

}

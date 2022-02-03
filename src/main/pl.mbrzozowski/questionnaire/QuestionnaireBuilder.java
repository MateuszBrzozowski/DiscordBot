package questionnaire;

import embed.EmbedSettings;
import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireBuilder {

    private final String COUNT_ANSWERS = "Razem głosów - ";
    private final String YES = "TAK";
    private final String NO = "NIE";
    private final String QUESTIONNAIRE = "Ankieta";
    private final String QUESTION = "Pytanie";
    private final String CREATED_BY = "Utworzono przez ";
    private final String ANSWERS = "Odpowiedzi";
    private final String END = "Zakończ";

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String authorID = RoleID.DEV_ID;
    private String channelID = null;
    private String question = null;
    private String messageID = null;
    private boolean isEnding = false;
    private List<Answer> answers = new ArrayList<>();
    private boolean isMultiple = false;
    private boolean isPublic = false;

    public QuestionnaireBuilder() {
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

    private void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    QuestionnaireBuilder setChannelID(String channelID) {
        this.channelID = channelID;
        return this;
    }

    QuestionnaireBuilder addAnswer(String answer) {
        if (answers.size() < 9) {
            Answer a = new Answer(answer, getEmoji(answers.size() + 1));
            answers.add(a);
        }
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


    String getAuthorID() {
        return authorID;
    }

    String getChannelID() {
        return channelID;
    }

    String getQuestion() {
        return question;
    }

    List<Answer> getAnswers() {
        return answers;
    }

    String getMessageID() {
        return messageID;
    }

    boolean isMultiple() {
        return isMultiple;
    }

    boolean isPublic() {
        return isPublic;
    }

    void build() {
        if (channelID != null && question != null) {
            if (answers.isEmpty()) {
                Answer answerYes = new Answer(YES, QuestionnaireStaticHelpers.EMOJI_YES);
                Answer answerNo = new Answer(NO, QuestionnaireStaticHelpers.EMOJI_NO);
                this.answers.add(answerYes);
                this.answers.add(answerNo);
            }
            buildGraphicsInterfaceAndQuestionnaire();
        }
    }

    private void buildGraphicsInterfaceAndQuestionnaire() {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelID);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(QUESTIONNAIRE);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        Color questionaireColor = new Color(59, 136, 195);
        builder.setColor(questionaireColor);
        builder.addField(QUESTION, question, false);
        builder.setFooter(CREATED_BY + Users.getUserNicknameFromID(authorID));
        builder.addField(ANSWERS, getAnswersField(), false);
        int allCountAnserws = getAllCountAnserws();
        builder.addField("", COUNT_ANSWERS + allCountAnserws, false);

        textChannel.sendMessage(builder.build()).queue(message -> {
            MessageEmbed mOld = message.getEmbeds().get(0);
            String msgID = message.getId();
            message.editMessage(mOld).setActionRow(
                    Button.danger("end_" + msgID, END)
            ).queue();
            addReactions(message);
            setMessageID(msgID);
            Repository.getQuestionnaires().addQuestionnaire(this);
        });
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
                if (answers.get(0).getAnswerID().equalsIgnoreCase(QuestionnaireStaticHelpers.EMOJI_YES)) {
                    message.addReaction(QuestionnaireStaticHelpers.EMOJI_YES).queue();
                    message.addReaction(QuestionnaireStaticHelpers.EMOJI_NO).queue();
                } else {
                    message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                    message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                }
                break;
            case 3:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                break;
            case 4:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_D).queue();
                break;
            case 5:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_D).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_E).queue();
                break;
            case 6:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_D).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_E).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_F).queue();
                break;
            case 7:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_D).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_E).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_F).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_G).queue();
                break;
            case 8:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_D).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_E).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_F).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_G).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_H).queue();
                break;
            case 9:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_C).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_D).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_E).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_F).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_G).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_H).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_I).queue();
                break;
            default:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_YES).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_NO).queue();
        }
    }

    private String getAnswersField() {
        String result = "";
        for (int i = 0; i < answers.size(); i++) {
            if (!answers.get(0).getAnswerID().equalsIgnoreCase(QuestionnaireStaticHelpers.EMOJI_YES)) {
                if (!isEnding) {
                    result += getEmoji(i + 1);
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


    void sortAnswers() {
        Sorter sorter = new Sorter();
        answers = sorter.sortQuestionnaireAnswersList(answers);
    }

    private String getEmoji(int i) {
        switch (i) {
            case 1:
                return QuestionnaireStaticHelpers.EMOJI_A;
            case 2:
                return QuestionnaireStaticHelpers.EMOJI_B;
            case 3:
                return QuestionnaireStaticHelpers.EMOJI_C;
            case 4:
                return QuestionnaireStaticHelpers.EMOJI_D;
            case 5:
                return QuestionnaireStaticHelpers.EMOJI_E;
            case 6:
                return QuestionnaireStaticHelpers.EMOJI_F;
            case 7:
                return QuestionnaireStaticHelpers.EMOJI_G;
            case 8:
                return QuestionnaireStaticHelpers.EMOJI_H;
            case 9:
                return QuestionnaireStaticHelpers.EMOJI_I;
            default:
                return "";
        }
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
            message.editMessage(m).queue();
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
            message.editMessage(m).queue();
        });
    }

}

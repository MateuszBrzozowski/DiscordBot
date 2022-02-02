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


    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String authorID = RoleID.DEV_ID;
    private String channelID = null;
    private String question = null;
    private String messageID = null;
    private List<String> answers = new ArrayList<>();

    public QuestionnaireBuilder setQuestion(String question) {
        this.question = question;
        return this;
    }

    public QuestionnaireBuilder setChannelID(String channelID) {
        this.channelID = channelID;
        return this;
    }

    public QuestionnaireBuilder addAnswer(String answer) {
        if (answers.size() < 9) {
            answers.add(answer);
        }
        return this;
    }

    public QuestionnaireBuilder setAuthorID(String userID) {
        this.authorID = userID;
        return this;
    }

    public void build() {
        if (channelID != null && question != null) {
            buildGraphicsInterfaceAndQuestionnaire();
        }

    }

    private void buildGraphicsInterfaceAndQuestionnaire() {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelID);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Ankieta");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        Color questionaire = new Color(59, 136, 195);
        builder.setColor(questionaire);
        builder.addField("Pytanie", question, false);
        builder.setFooter("Utworzono przez " + Users.getUserNicknameFromID(authorID));
        builder.addField("Odpowiedzi", getAnswersField(), false);
        builder.addField("", "Odpowiedziało - 0", false);

        textChannel.sendMessage(builder.build()).queue(message -> {
            MessageEmbed mOld = message.getEmbeds().get(0);
            String msgID = message.getId();
            message.editMessage(mOld).setActionRow(
                    Button.danger("end_" + msgID, "Zakończ")
            ).queue();
            addReactions(message, mOld, msgID);
            setMessageID(msgID);
            Repository.getQuestionnaires().addQuestionnaire(this);
        });
    }

    private void addReactions(Message message, MessageEmbed mOld, String msgID) {
        switch (answers.size()) {
            case 2:
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_A).queue();
                message.addReaction(QuestionnaireStaticHelpers.EMOJI_B).queue();
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
        if (answers.size() < 2) {
            return "TAK **- 0 Głosów**\nNIE **- 0 Głosów**";
        } else {
            return getEmptyPublicAnswersToEmbed();
        }
    }

    private String getEmptyPublicAnswersToEmbed() {
        String result = "";
        for (int i = 0; i < answers.size(); i++) {
            result += getEmoji(i + 1);
            result += " " + answers.get(i) + " **- 0 Głosów**\n";
        }
        return result;
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

    public String getAuthorID() {
        return authorID;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
}

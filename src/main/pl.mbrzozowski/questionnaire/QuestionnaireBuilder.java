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
    private List<String> answers = new ArrayList<>();
    private String EMOJI_A = "\uD83C\uDDE6";
    private String EMOJI_B = "\uD83C\uDDE7";
    private String EMOJI_C = "\uD83C\uDDE8";
    private String EMOJI_D = "\uD83C\uDDE9";
    private String EMOJI_E = "\uD83C\uDDEA";
    private String EMOJI_F = "\uD83C\uDDEB";
    private String EMOJI_G = "\uD83C\uDDEC";
    private String EMOJI_H = "\uD83C\uDDED";
    private String EMOJI_I = "\uD83C\uDDEE";
    private String EMOJI_YES = "U+2705";
    private String EMOJI_NO = "U+274C";

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
        //sprawdzic czy sa odpowiedzi, jezeli nie to robimy prosta ankiete TAK/NIE
        //sprawdzic odpowiedzi czy ktoras jest dluzsza niz 80 znakow, jezeli tak to odpowiedzi na embed, jezeli nie to na buttonach.
        //glosowanie nie jest jawne. chyba ze jest ustawione na jawne
        if (channelID != null && question != null) {
            JDA jda = Repository.getJda();
            TextChannel textChannel = jda.getTextChannelById(channelID);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Ankieta");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            Color questionaire = new Color(59, 136, 195);
            builder.setColor(questionaire);
            builder.addField("Pytanie", question, false);
            builder.setFooter("Utworzono przez " + Users.getUserNicknameFromID(authorID));

            builder.addField("Odpowiedzi", getAnswers(), false);
            builder.addField("", "Odpowiedziało - 0", false);

            textChannel.sendMessage(builder.build()).queue(message -> {
                MessageEmbed mOld = message.getEmbeds().get(0);
                String msgID = message.getId();
                message.editMessage(mOld).setActionRow(
                        Button.danger("end_" + msgID, "Zakończ")
                ).queue();
                addReactions(message, mOld, msgID);
            });
        }

    }

    private void addReactions(Message message, MessageEmbed mOld, String msgID) {
        switch (answers.size()) {
            case 2:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                break;
            case 3:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                break;
            case 4:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                message.addReaction(EMOJI_D).queue();
                break;
            case 5:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                message.addReaction(EMOJI_D).queue();
                message.addReaction(EMOJI_E).queue();
                break;
            case 6:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                message.addReaction(EMOJI_D).queue();
                message.addReaction(EMOJI_E).queue();
                message.addReaction(EMOJI_F).queue();
                break;
            case 7:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                message.addReaction(EMOJI_D).queue();
                message.addReaction(EMOJI_E).queue();
                message.addReaction(EMOJI_F).queue();
                message.addReaction(EMOJI_G).queue();
                break;
            case 8:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                message.addReaction(EMOJI_D).queue();
                message.addReaction(EMOJI_E).queue();
                message.addReaction(EMOJI_F).queue();
                message.addReaction(EMOJI_G).queue();
                message.addReaction(EMOJI_H).queue();
                break;
            case 9:
                message.addReaction(EMOJI_A).queue();
                message.addReaction(EMOJI_B).queue();
                message.addReaction(EMOJI_C).queue();
                message.addReaction(EMOJI_D).queue();
                message.addReaction(EMOJI_E).queue();
                message.addReaction(EMOJI_F).queue();
                message.addReaction(EMOJI_G).queue();
                message.addReaction(EMOJI_H).queue();
                message.addReaction(EMOJI_I).queue();
                break;
            default:
                message.addReaction(EMOJI_YES).queue();
                message.addReaction(EMOJI_NO).queue();
        }
    }

    private String getAnswers() {
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
                return EMOJI_A;
            case 2:
                return EMOJI_B;
            case 3:
                return EMOJI_C;
            case 4:
                return EMOJI_D;
            case 5:
                return EMOJI_E;
            case 6:
                return EMOJI_F;
            case 7:
                return EMOJI_G;
            case 8:
                return EMOJI_H;
            case 9:
                return EMOJI_I;
            default:
                return "";
        }
    }

}

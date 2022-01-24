package questionnaire;

import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
    private boolean isPublic = false;
    private String authorID = RoleID.DEV_ID;
    private String channelID = null;
    private String question = null;
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
        if (answers.size() < 5) {
            answers.add(answer);
        }
        return this;
    }

    public QuestionnaireBuilder setAuthorID(String userID) {
        this.authorID = userID;
        return this;
    }

    public QuestionnaireBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
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
            builder.setColor(Color.YELLOW);
            builder.addField("Pytanie", question, false);
            builder.setFooter("Utworzono przez " + Users.getUserNicknameFromID(authorID));
            if (!answers.isEmpty()) {
                if (isPublic) {
                    builder.addField("Odpowiedzi", getEmptyPublicAnswersToEmbed(), false);
                } else {
                    builder.addField("Odpowiedzi", getEmptyAnswersToEmbed(), false);
                    builder.addField("", "Odpowiedziało (0)", false);
                }

            } else {
                if (isPublic) {
                    builder.addField("Odpowiedzi", "(0) TAK\n(0) NIE", false);
                }
                builder.addField("", "Odpowiedziało (0)", false);
                textChannel.sendMessage(builder.build()).queue(message -> {
                    MessageEmbed mOld = message.getEmbeds().get(0);
                    String msgID = message.getId();
                    message.editMessage(mOld).setActionRow(
                            Button.primary("A_" + msgID, "TAK"),
                            Button.primary("B_" + msgID, "NIE"),
                            Button.danger("end_" + msgID, "Zakończ")).queue();
                });
                //prosta ankieta - jest pytanie i odpowiedzi TAK NIE
            }


        }
    }

    private String getEmptyAnswersToEmbed() {
        String result = "";
        for (int i = 0; i < 5; i++) {
            result += answers.get(i) + "\n";
        }
        return result;
    }

    private String getEmptyPublicAnswersToEmbed() {
        String result = "";
        for (int i = 0; i < 5; i++) {
            result += "(0) " + answers.get(i) + "\n";
        }
        return result;
    }

}

package recrut;

import embed.EmbedSettings;
import helpers.CategoryAndChannelID;
import helpers.ComponentId;
import helpers.RangerLogger;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;

import java.awt.*;

public class RecruitOpinions {


    public void openForm(@NotNull ButtonInteractionEvent event) {
        TextInput recruitName = TextInput.create(ComponentId.RECRUIT_NAME, "Nick Rekruta", TextInputStyle.SHORT)
                .setMaxLength(100)
                .setPlaceholder("Nick")
                .build();
        TextInput opinion = TextInput.create(ComponentId.RECRUIT_OPINION_TEXT, "opinia", TextInputStyle.PARAGRAPH)
                .setRequiredRange(10, 1024)
                .setPlaceholder("Opinia")
                .build();
        Modal modal = Modal.create(ComponentId.RECRUIT_OPINION_MODAL, "Rekrut opinie")
                .addActionRows(ActionRow.of(recruitName), ActionRow.of(opinion))
                .build();
        event.replyModal(modal).queue();
    }

    public void submitForm(ModalInteractionEvent event) {
        String recruitName = event.getValue(ComponentId.RECRUIT_NAME).getAsString();
        String opinion = event.getValue(ComponentId.RECRUIT_OPINION_TEXT).getAsString();
        String userNameWhoSendingOpinion = Users.getUserNicknameFromID(event.getUser().getId());
        event.deferEdit().queue();
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(CategoryAndChannelID.CHANNEL_RECRUITS_OPINIONS);
        sendOpinionToChannel(textChannel, userNameWhoSendingOpinion, recruitName, opinion);
    }

    private void sendOpinionToChannel(TextChannel textChannel, String userNameWhoSendingOpinion, String recruitName, String opinion) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        try {
            builder.setTitle("U??ytkownik: " + userNameWhoSendingOpinion + " wystawi?? opini??.");
            builder.setDescription("Na temat rekruta: **" + recruitName + "**");
            builder.addField("", opinion, false);
            textChannel.sendMessageEmbeds(builder.build()).queue();
        } catch (IllegalArgumentException e) {
            RangerLogger.info("Rekrut opinia - IllegalArgumentException " + e.getMessage());
        }


    }
}

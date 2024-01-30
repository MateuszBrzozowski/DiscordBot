package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.util.Objects;

@Slf4j
public class RecruitOpinions {

    public static void openOpinionAboutRecruit(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - open opinion about recruit");
        TextInput recruitName = TextInput.create(ComponentId.RECRUIT_NAME, "Nick Rekruta", TextInputStyle.SHORT)
                .setMaxLength(100)
                .setPlaceholder("Nick")
                .build();
        TextInput opinion = TextInput.create(ComponentId.RECRUIT_OPINION_TEXT, "opinia", TextInputStyle.PARAGRAPH)
                .setRequiredRange(10, 1024)
                .setPlaceholder("Opinia")
                .build();
        Modal modal = Modal.create(ComponentId.MODAL_RECRUIT_OPINION, "Rekrut opinie")
                .addActionRows(ActionRow.of(recruitName), ActionRow.of(opinion))
                .build();
        event.replyModal(modal).queue();
    }

    public static void submitOpinionAboutRecruit(@NotNull ModalInteractionEvent event) {
        String recruitName = Objects.requireNonNull(event.getValue(ComponentId.RECRUIT_NAME)).getAsString();
        String opinion = Objects.requireNonNull(event.getValue(ComponentId.RECRUIT_OPINION_TEXT)).getAsString();
        String userNameWhoSendingOpinion = Users.getUserNicknameFromID(event.getUser().getId());
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(CategoryAndChannelID.CHANNEL_RECRUITS_OPINIONS);
        if (textChannel != null) {
            sendOpinionToChannel(textChannel, userNameWhoSendingOpinion, recruitName, opinion);
        }
        event.deferEdit().queue();
        log.info("{} - send opinion({}) about recruit({})", event.getUser(), opinion, recruitName);
    }

    public static void openAnonymousComplaints(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - open anonymous complaints");
        TextInput text = TextInput.create(ComponentId.MODAL_COMPLAINT_TEXT, "Wiadomość", TextInputStyle.PARAGRAPH)
                .setRequiredRange(10, 1000)
                .setPlaceholder("Opisz sytuacje.")
                .build();
        Modal modal = Modal.create(ComponentId.MODAL_COMPLAINTS, "Anonimowe zgłoszenie")
                .addActionRows(ActionRow.of(text))
                .build();
        event.replyModal(modal).queue();
    }

    public static void submitAnonymousComplaints(@NotNull ModalInteractionEvent event) {
        String text = Objects.requireNonNull(event.getValue(ComponentId.MODAL_COMPLAINT_TEXT)).getAsString();
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(CategoryAndChannelID.CHANNEL_DRILL_INSTRUCTOR_HQ);
        if (textChannel != null) {
            sendMessage(textChannel, event.getUser(), text);
        }
        event.deferEdit().queue();
        log.info("{} - send anonymous complaints", event.getUser());
    }

    private static void sendMessage(@NotNull TextChannel textChannel, User user, String text) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Anonimowy donos!");
        builder.setDescription("**Wiadomość:**\n" + text);
        textChannel.sendMessage("<@&" + RoleID.CLAN_COUNCIL + ">")
                .setEmbeds(builder.build())
                .queue(message -> log.info("{} -(nickname:{}) send anonymous complaints",
                        user,
                        Users.getUserNicknameFromID(user.getId())));
    }

    private static void sendOpinionToChannel(@NotNull TextChannel textChannel, String userNameWhoSendingOpinion, String recruitName, String opinion) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        String title = "Użytkownik: " + userNameWhoSendingOpinion + " wystawił opinię.";
        String description = "Na temat rekruta: **" + recruitName + "**";
        title = title.substring(0, Math.min(title.length(), MessageEmbed.TITLE_MAX_LENGTH));
        description = description.substring(0, Math.min(description.length(), MessageEmbed.DESCRIPTION_MAX_LENGTH));
        opinion = opinion.substring(0, Math.min(opinion.length(), MessageEmbed.VALUE_MAX_LENGTH));
        builder.setTitle(title);
        builder.setDescription(description);
        builder.addField("", opinion, false);
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }
}

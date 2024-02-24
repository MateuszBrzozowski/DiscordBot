package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.guild.ContextCommands;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.model.ContextCommand;
import pl.mbrzozowski.ranger.model.ErrorMessages;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class RecruitOpinions implements ContextCommand {

    private final Map<User, User> activeOpinionsAboutRecruit = new HashMap<>();
    private static final RecruitOpinions instance = new RecruitOpinions();

    private RecruitOpinions() {
    }

    public static RecruitOpinions getInstance() {
        return instance;
    }

    private void openOpinionAboutRecruit(@NotNull GenericContextInteractionEvent<?> event, User user, User recruit) {
        activeOpinionsAboutRecruit.put(user, recruit);
        log.info(event.getUser() + " - open opinion about recruit");
        TextInput opinion = TextInput.create(ComponentId.RECRUIT_OPINION_TEXT, "opinia", TextInputStyle.PARAGRAPH)
                .setRequiredRange(10, 1024)
                .setPlaceholder("Opinia o " + Users.getUserNicknameFromID(recruit.getId()))
                .build();
        Modal modal = Modal.create(ComponentId.MODAL_RECRUIT_OPINION, "Rekrut opinia")
                .addComponents(ActionRow.of(opinion))
                .build();
        event.replyModal(modal).queue();
    }

    public void submitOpinionAboutRecruit(@NotNull ModalInteractionEvent event) {
        User user = event.getUser();
        User recruit = activeOpinionsAboutRecruit.get(user);
        if (recruit == null) {
            event.reply(ErrorMessages.UNKNOWN_EXCEPTIONS.getMessage()).setEphemeral(true).queue();
            log.warn("Recruit user is null");
            return;
        }
        String opinion = Objects.requireNonNull(event.getValue(ComponentId.RECRUIT_OPINION_TEXT)).getAsString();
        TextChannel textChannel = RangersGuild.getTextChannel(RangersGuild.ChannelsId.RECRUIT_OPINIONS);
        if (textChannel != null) {
            new Thread(() -> sendOpinionToChannel(textChannel, user, recruit, opinion)).start();
            event.reply("Opinia wysłana poprawnie. Dziękujemy!").setEphemeral(true).queue();
        } else {
            event.reply(ErrorMessages.UNKNOWN_EXCEPTIONS.getMessage()).setEphemeral(true).queue();
            log.error("Text channel for recruit opinions is null.");
        }
    }

    public void openAnonymousComplaints(@NotNull ButtonInteractionEvent event) {
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

    public void submitAnonymousComplaints(@NotNull ModalInteractionEvent event) {
        String text = Objects.requireNonNull(event.getValue(ComponentId.MODAL_COMPLAINT_TEXT)).getAsString();
        TextChannel textChannel = RangersGuild.getTextChannel(RangersGuild.ChannelsId.DRILL_INSTRUCTOR_HQ);
        if (textChannel != null) {
            new Thread(() -> sendMessage(textChannel, event.getUser(), text)).start();
            event.reply("Dziękujemy za zgłoszenie!").setEphemeral(true).queue();
            log.info("{} - send anonymous complaints", event.getUser());
            return;
        }
        event.reply(ErrorMessages.UNKNOWN_EXCEPTIONS.getMessage()).setEphemeral(true).queue();
        log.error("Text channel is null");
    }

    private void sendMessage(@NotNull TextChannel textChannel, User user, String text) {
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

    private void sendOpinionToChannel(@NotNull TextChannel textChannel, @NotNull User user, @NotNull User recruit, String opinion) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setDescription("### Użytkownik: " + user.getAsMention() + " wystawił opinię.\n" +
                "Na temat rekruta: " + recruit.getAsMention());
        builder.addField("", opinion, false);
        textChannel.sendMessageEmbeds(builder.build())
                .queue(message -> log.info("Opinion sent {}", message), throwable -> log.error("Can not send opinion"));
    }

    public void opinion(@NotNull UserContextInteractionEvent event) {
        User user = event.getUser();
        User recruit = event.getTarget();
        if (canNotSendOpinion(event, user, recruit)) return;
        openOpinionAboutRecruit(event, user, recruit);
    }

    public void opinion(@NotNull MessageContextInteractionEvent event) {
        User user = event.getUser();
        User recruit = event.getTarget().getAuthor();
        if (canNotSendOpinion(event, user, recruit)) return;
        openOpinionAboutRecruit(event, user, recruit);
    }

    private boolean canNotSendOpinion(@NotNull GenericContextInteractionEvent<?> event, @NotNull User user, @NotNull User recruit) {
        boolean isClanMember = Users.hasUserRole(user.getId(), RoleID.CLAN_MEMBER_ID);
        if (!isClanMember) {
            event.reply(ErrorMessages.NO_PERMISSIONS.getMessage()).setEphemeral(true).queue();
            log.warn("User {} is not clan member. He can use context menu", user);
            return true;
        }
        boolean isRecruit = Users.hasUserRole(recruit.getId(), RoleID.RECRUIT_ID);
        if (!isRecruit) {
            event.reply("Użytkownik " + recruit.getAsMention() + " nie jest rekrutem").setEphemeral(true).queue();
            log.info("User {} is not recruit", recruit);
            return true;
        }
        return false;
    }

    @Override
    public void getContextCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.context(Command.Type.USER, ContextCommands.RECRUIT_OPINION.getName()));
        commandData.add(Commands.message(ContextCommands.RECRUIT_OPINION.getName()));
    }
}

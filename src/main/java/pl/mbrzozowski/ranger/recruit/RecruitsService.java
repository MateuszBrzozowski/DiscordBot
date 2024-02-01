package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.repository.main.RecruitBlackListRepository;
import pl.mbrzozowski.ranger.repository.main.RecruitRepository;
import pl.mbrzozowski.ranger.repository.main.WaitingRecruitRepository;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static net.dv8tion.jda.api.entities.MessageEmbed.Field;
import static pl.mbrzozowski.ranger.helpers.ComponentId.*;

@Service
@Slf4j
public class RecruitsService {

    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_MENTION_EVERYONE);
    private final Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);
    private final RecruitRepository recruitRepository;
    private final RecruitBlackListRepository recruitBlackListRepository;
    private final WaitingRecruitRepository waitingRecruitRepository;
    private final int MAX_CHANNELS = 50;

    public RecruitsService(RecruitRepository recruitRepository,
                           RecruitBlackListRepository recruitBlackListRepository,
                           WaitingRecruitRepository waitingRecruitRepository) {
        this.recruitRepository = recruitRepository;
        this.recruitBlackListRepository = recruitBlackListRepository;
        this.waitingRecruitRepository = waitingRecruitRepository;
    }

    /**
     * @param userName Nazwa użytkownika
     * @param userID   ID użytkownika
     */
    public void createChannelForNewRecruit(String userName, String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            throw new NullPointerException("Guild by RangersPL id is null");
        }
        Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_RECRUT_ID);
        guild.createTextChannel("rekrut-" + userName, category)
                .addPermissionOverride(guild.getPublicRole(), null, permissions)
                .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_COUNCIL), permissions, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permViewChannel, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.DRILL_INSTRUCTOR_ID), permissions, null)
                .queue(textChannel -> {
                    log.info("created text channel(name={}, id={})", textChannel.getName(), textChannel.getId());
                    sendWelcomeMessage(userID, textChannel);
                    add(userID, userName, textChannel.getId());
                });
        checkCountOfRecruitChannels();
    }

    private void checkCountOfRecruitChannels() {
        int channelsInCategory = howManyChannelsInCategory();
        if (channelsInCategory == MAX_CHANNELS) {
            EmbedInfo.warningMaxRecruits();
            log.info("Maximum number of recruitment channels achieved. Warning sent.");
        } else if (channelsInCategory >= MAX_CHANNELS - 2) {
            EmbedInfo.warningFewSlots();
            log.info("Low number of recruitment channels. Warning sent.");
        }
    }

    public void sendWelcomeMessage(String userID, @NotNull TextChannel textChannel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setDescription("## Obowiązkowo:");
        builder.addField("", """
                        **1. Przeczytaj manual - Najważniejsze zasady gry w Rangers Polska**
                        > [Manual](https://drive.google.com/file/d/18uefRZx5vIZrD-7wYQqgAk-JlYDgfQzq/view)\s

                        **2. Jeżeli zaczynasz przygodę ze Squadem przeczytaj poradnik:**\s
                        > [Poradnik](https://steamcommunity.com/sharedfiles/filedetails/?id=2878029717)

                        **3. TeamSpeak3:**
                        ```fix
                         ts.rangerspolska.pl:6969
                        ```""",
                false);
        textChannel.sendMessage("Cześć <@" + userID + ">!\nCieszymy się, że złożyłeś podanie do klanu.\n" +
                        "<@&" + RoleID.HEAD_DRILL_INSTRUCTOR_ID + ">\n" +
                        "<@&" + RoleID.DRILL_INSTRUCTOR_ID + ">")
                .setEmbeds(builder.build())
                .setActionRow(
                        Button.primary(ComponentId.RECRUIT_ACCEPTED, "\u200E"),
                        Button.secondary(ComponentId.RECRUIT_NOT_ACCEPTED, "\u200E"),
                        Button.success(ComponentId.RECRUIT_POSITIVE, "\u200E"),
                        Button.danger(ComponentId.RECRUIT_NEGATIVE, "\u200E"))
                .queue();
        EmbedInfo.recruitAnonymousComplaintsFormOpening(textChannel);
    }

    public void newPodanie(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - New recruit application");
        String userId = event.getUser().getId();
        if (Users.memberOnGuildShorterThan(userId, 10)) {
            ResponseMessage.noReqTimeOnServer(event);
            return;
        }
        if (Users.hasUserRole(userId, RoleID.CLAN_MEMBER_ID)) {
            ResponseMessage.userIsInClanMember(event);
            return;
        }
        if (hasRecruitChannel(userId)) {
            ResponseMessage.userHaveRecruitChannel(event);
            return;
        }
        if (userBlackList(userId)) {
            ResponseMessage.userBlackList(event);
            return;
        }
        if (isAwaitingToConfirmForm(userId)) {
            ResponseMessage.awaitingConfirmForm(event);
            return;
        }
        confirmMessage(event);
    }

    private boolean isAwaitingToConfirmForm(String userId) {
        Optional<WaitingRecruit> optionalWaitingRecruit = waitingRecruitRepository.findByUserId(userId);
        return optionalWaitingRecruit.isPresent();
    }

    private boolean userBlackList(String userID) {
        Optional<RecruitBlackList> recruitBlackListOptional = recruitBlackListRepository.findByUserId(userID);
        return recruitBlackListOptional.isPresent();
    }

    private boolean isMaxRecruits() {
        int howManyChannelsNow = howManyChannelsInCategory();
        return howManyChannelsNow >= MAX_CHANNELS;
    }

    private int howManyChannelsInCategory() {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_RECRUT_ID);
            if (category != null) {
                return category.getChannels().size();
            }
        }
        return MAX_CHANNELS + 1;
    }

    private void confirmMessage(@NotNull ButtonInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setDescription("## Formularz rekrutacyjny\n" +
                "- Uzupełnij formularz rekrutacyjny z linku poniżej.\n" +
                "- Potwierdź przesłany formularz poniższym przyciskiem.\n" +
                "- Po zweryfikowaniu Twojego formularza zostanie utworzony kanał rekrutacyjny i skontaktuje się z Tobą jeden z naszych <@&" + RoleID.DRILL_INSTRUCTOR_ID + ">");
        builder.addField("", "**[Formularz](https://docs.google.com/forms/d/e/1FAIpQLSeWVDY4p5-RlWA6Ug_JMeS1asJVLDJHcblqCNRPuXC87kr8lA/viewform)**", false);
        event.replyEmbeds(builder.build()).setComponents(
                        ActionRow.of(Button.success(CONFIRM_FORM_SEND + event.getUser().getId(), "Potwierdzam wysłanie formularza")))
                .setEphemeral(true)
                .queue();
    }

    public void confirm(@NotNull ButtonInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("## Formularz rekrutacyjny");
        builder.setColor(Color.GREEN);
        builder.addField("Dziękujemy za przesłany formularz", "Oczekuj na utworzenie kanału rekrutacyjnego i kontakt od naszego <@&" + RoleID.DRILL_INSTRUCTOR_ID + "> ", false);
        event.getInteraction().deferEdit().queue();
        event.getMessage().editMessageEmbeds(builder.build()).setComponents().queue();
        WaitingRecruit waitingRecruit = new WaitingRecruit(event.getUser().getId());
        save(waitingRecruit);
        sendInformationAboutNewForm(event);
    }

    private void sendInformationAboutNewForm(@NotNull ButtonInteractionEvent event) {
        String userId = event.getComponentId().substring(CONFIRM_FORM_SEND.length());
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(CategoryAndChannelID.CHANNEL_DRILL_INSTRUCTOR_HQ);
        if (textChannel == null) {
            throw new IllegalStateException("Text channel not exist id=" + CategoryAndChannelID.CHANNEL_DRILL_INSTRUCTOR_HQ);
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setDescription("## Nowy formularz\n" +
                Converter.LocalDateTimeToTimestampDateTimeLongFormat(LocalDateTime.now(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS))));
        builder.addField("Rekrut:", "User: <@" + userId + ">\n" +
                "Server nickname: " + Users.getUserNicknameFromID(userId), false);
        builder.addField("", "[Arkusz](https://docs.google.com/spreadsheets/d/1GF7BK03K_elLYrVqnfB2RFFI3pCCGcN2D6AF6G61Ta4/edit?usp=sharing)", false);
        textChannel.sendMessage("<@&" + RoleID.DRILL_INSTRUCTOR_ID + ">")
                .setEmbeds(builder.build())
                .addActionRow(Button.success(CONFIRM_FORM_RECEIVED + userId, "Potwierdź"),
                        Button.danger(DECLINE_FORM_SEND + userId, "Odrzuć"))
                .queue();
    }

    private boolean hasRecruitChannel(String userId) {
        Optional<Recruit> recruitOptional = recruitRepository.findByUserId(userId);
        return recruitOptional.isPresent();
    }


    private Optional<Recruit> findByUserId(String userId) {
        return recruitRepository.findByUserId(userId);
    }

    private Optional<Recruit> findByChannelId(String channelId) {
        return recruitRepository.findByChannelId(channelId);
    }

    public List<Recruit> findAllWithChannel() {
        return recruitRepository.findAllWithChannelId();
    }

    private void save(WaitingRecruit waitingRecruit) {
        waitingRecruitRepository.save(waitingRecruit);
    }

    private void deleteByUserId(String userId) {
        waitingRecruitRepository.deleteByUserId(userId);
    }

    private void add(String userId, String userName, String channelID) {
        Recruit recruit = Recruit.builder()
                .userId(userId)
                .name(userName)
                .channelId(channelID)
                .toApply(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime())
                .isCloseChannel(false)
                .build();
        recruitRepository.save(recruit);
        log.info("user(name={}, id={}) added to DB as recruit", userName, userId);
    }

    public void deleteChannelByID(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            removeRecruitRoleFromUserID(recruit.getUserId());
            recruitRepository.delete(recruit);
            log.info("(channelId={}) - has been removed", channelID);
        }
    }

    private void addRoleRecruit(String userId) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Member member = guild.getMemberById(userId);
            Role roleRecruit = guild.getRoleById(RoleID.RECRUIT_ID);
            boolean hasRoleRecruit = Users.hasUserRole(userId, RoleID.RECRUIT_ID);
            if (!hasRoleRecruit && roleRecruit != null && member != null) {
                guild.addRoleToMember(member, roleRecruit)
                        .queue(unused -> log.info("Added role recruit to user(userId={}", userId));
            }
        }
    }

    public void removeRecruitRoleFromUserID(String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Member member = guild.getMemberById(userID);
            Role roleRecruit = guild.getRoleById(RoleID.RECRUIT_ID);
            boolean hasRoleRecruit = Users.hasUserRole(userID, RoleID.RECRUIT_ID);
            if (hasRoleRecruit && roleRecruit != null && member != null) {
                guild.removeRoleFromMember(member, roleRecruit).queue();
            }
        }
    }

    private void addClanMemberRoleFromUserId(String userId) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Member member = guild.getMemberById(userId);
            Role roleClanMember = guild.getRoleById(RoleID.CLAN_MEMBER_ID);
            boolean hasUserRole = Users.hasUserRole(userId, RoleID.CLAN_MEMBER_ID);
            if (!hasUserRole && roleClanMember != null && member != null) {
                guild.addRoleToMember(member, roleClanMember).queue();
            }
        }
    }

    public void deleteChannel(@NotNull Recruit recruit) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(recruit.getChannelId());
        if (textChannel != null) {
            textChannel.delete().reason("Rekrutacja zakończona.").queue();
            recruitRepository.delete(recruit);
            log.info(textChannel.getId() + " - channel deleted");
        }
    }

    public void accepted(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - use recruit accept button");
        Optional<Recruit> recruitOptional = findByChannelId(event.getChannel().getId());
        if (recruitOptional.isEmpty()) {
            ResponseMessage.operationNotPossible(event);
            return;
        }
        Recruit recruit = recruitOptional.get();
        if (validToAccept(event, recruit)) {
            return;
        }
        event.deferEdit().queue();
        addRoleRecruit(recruit.getUserId());
        addRecruitTag(event.getGuild(), recruit.getUserId());
        setYellowCircleInChannelName(event.getChannel().asTextChannel());
        EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getChannel().asTextChannel());
        recruit.setStartRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        recruitRepository.save(recruit);
        log.info("{} - accepted recruit (channelId={}, channelName={})",
                event.getUser(),
                event.getChannel().getId(),
                event.getChannel().getName());
    }

    private boolean validToAccept(@NotNull ButtonInteractionEvent event, @NotNull Recruit recruit) {
        if (recruit.getStartRecruitment() != null) {
            ResponseMessage.recruitHasBeenAccepted(event);
            return true;
        } else if (recruit.getRecruitmentResult() != null) {
            ResponseMessage.recruitHasBeenRejected(event);
            return true;
        }
        return false;
    }

    public void accepted(@NotNull ButtonInteractionEvent event, final boolean isAdmin) {
        if (isAdmin) {
            accepted(event);
        } else {
            ResponseMessage.noPermission(event);
        }
    }

    public void positiveResult(@NotNull ButtonInteractionEvent event) {
        boolean result = positiveResult(event.getUser().getId(), event.getChannel().asTextChannel());
        if (result) {
            event.deferEdit().queue();
            log.info(event.getUser() + " - Send positive result for recruit");
        } else {
            ResponseMessage.operationNotPossible(event);
        }
    }

    public void positiveResult(@NotNull ButtonInteractionEvent event, final boolean isAdmin) {
        if (isAdmin) {
            positiveResult(event);
        } else {
            ResponseMessage.noPermission(event);
        }
    }

    public boolean positiveResult(String drillId, @NotNull TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getStartRecruitment() == null) {
                return false;
            }
            if (recruit.getEndRecruitment() == null
                    && recruit.getRecruitmentResult() == null) {
                Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
                if (guild != null) {
                    removeSmallRInTag(recruit.getUserId(), guild);
                    removeRecruitRoleFromUserID(recruit.getUserId());
                    addClanMemberRoleFromUserId(recruit.getUserId());
                    recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
                    recruit.setRecruitmentResult(RecruitmentResult.POSITIVE);
                    recruitRepository.save(recruit);
                    setGreenCircleInChannelName(channel);
                    EmbedInfo.endPositive(drillId, recruit.getUserId(), channel);
                    return true;
                }
            }
        }
        return false;
    }

    public void negativeResult(@NotNull ButtonInteractionEvent event) {
        boolean result = negativeResult(event.getUser().getId(), event.getChannel().asTextChannel());
        if (result) {
            event.deferEdit().queue();
            log.info(event.getUser() + " - Send negative result for recruit");
        } else {
            ResponseMessage.operationNotPossible(event);
        }
    }

    public void negativeResult(@NotNull ButtonInteractionEvent event, final boolean isAdmin) {
        if (isAdmin) {
            negativeResult(event);
        } else {
            ResponseMessage.noPermission(event);
        }
    }

    public boolean negativeResult(String drillId, @NotNull TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getStartRecruitment() == null) {
                return false;
            }
            if (recruit.getEndRecruitment() == null && recruit.getRecruitmentResult() == null) {
                Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
                if (guild != null) {
                    removeTagFromNick(recruit.getUserId(), guild);
                    removeRecruitRoleFromUserID(recruit.getUserId());
                    recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
                    recruit.setRecruitmentResult(RecruitmentResult.NEGATIVE);
                    recruitRepository.save(recruit);
                    setRedCircleInChannelName(channel);
                    EmbedInfo.endNegative(drillId, recruit.getUserId(), channel);
                    return true;
                }
            }
        }
        return false;
    }

    private void setRedCircleInChannelName(@NotNull TextChannel channel) {
        String oldName = channel.getName();
        oldName = removeAnyPrefixCircle(oldName);
        channel.getManager().setName(EmbedSettings.RED_CIRCLE + oldName).queue();
    }

    private void setGreenCircleInChannelName(@NotNull TextChannel channel) {
        String oldName = channel.getName();
        oldName = removeAnyPrefixCircle(oldName);
        channel.getManager().setName(EmbedSettings.GREEN_CIRCLE + oldName).queue();
    }

    private void setYellowCircleInChannelName(@NotNull TextChannel channel) {
        String oldName = channel.getName();
        oldName = removeAnyPrefixCircle(oldName);
        channel.getManager().setName(EmbedSettings.YELLOW_CIRCLE + oldName).queue();
    }

    @NotNull
    private String removeAnyPrefixCircle(@NotNull String oldName) {
        if (oldName.contains(EmbedSettings.YELLOW_CIRCLE)) {
            oldName = oldName.replaceAll(EmbedSettings.YELLOW_CIRCLE, "");
        }
        if (oldName.contains(EmbedSettings.RED_CIRCLE)) {
            oldName = oldName.replaceAll(EmbedSettings.RED_CIRCLE, "");
        }
        if (oldName.contains(EmbedSettings.GREEN_CIRCLE)) {
            oldName = oldName.replaceAll(EmbedSettings.GREEN_CIRCLE, "");
        }
        return oldName;
    }

    private void removeSmallRInTag(@NotNull String userId, @NotNull Guild guild) {
        String userNickname = Users.getUserNicknameFromID(userId);
        if (userNickname != null && userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "<RangersPL>");
            Member member = guild.getMemberById(userId);
            if (member != null) {
                member.modifyNickname(userNickname).queue();
            }
        }
    }

    private void removeTagFromNick(@NotNull String userId, @NotNull Guild guild) {
        String userNickname = Users.getUserNicknameFromID(userId);
        if (userNickname != null && userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "");
            Member member = guild.getMemberById(userId);
            if (member != null) {
                member.modifyNickname(userNickname).queue();
            }
        }
    }

    private void addRecruitTag(Guild guild, @NotNull String userId) {
        String nicknameOld = Users.getUserNicknameFromID(userId);
        if (!isNicknameRNGSuffix(nicknameOld)) {
            Member member = guild.getMemberById(userId);
            if (member != null) {
                member.modifyNickname(nicknameOld + "<rRangersPL>")
                        .queue(unused -> log.info("Add recruit tag to user(userId={})", userId));
            }
        }
    }

    protected boolean isNicknameRNGSuffix(String nickname) {
        if (StringUtils.isBlank(nickname)) {
            return true;
        }
        nickname = nickname.replace(" ", "");
        if (nickname.endsWith("<rRangersPL>")) return true;
        else return nickname.endsWith("<RangersPL>");
    }

    public void recruitNotAccepted(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - use recruit not accepted ");
        Optional<Recruit> recruitOptional = findByChannelId(event.getChannel().getId());
        if (recruitOptional.isEmpty()) {
            ResponseMessage.operationNotPossible(event);
            return;
        }
        Recruit recruit = recruitOptional.get();
        if (validToNotAccept(event, recruit)) {
            return;
        }
        event.deferEdit().queue();
        setRedCircleInChannelName(event.getChannel().asTextChannel());
        recruit.setIsCloseChannel(true);
        recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        recruit.setRecruitmentResult(RecruitmentResult.NEGATIVE);
        recruitRepository.save(recruit);
        EmbedInfo.recruitNotAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getChannel().asTextChannel());
        log.info("{} - not accepted recruit (channelId={}, channelName={})",
                event.getUser(),
                event.getChannel().getId(),
                event.getChannel().getName());
    }

    private boolean validToNotAccept(@NotNull ButtonInteractionEvent event, @NotNull Recruit recruit) {
        if (recruit.getRecruitmentResult() != null) {
            ResponseMessage.recruitHasBeenRejected(event);
            return true;
        } else if (recruit.getStartRecruitment() != null) {
            ResponseMessage.recruitHasBeenAccepted(event);
            return true;
        }
        return false;
    }

    public void recruitNotAccepted(@NotNull ButtonInteractionEvent event, final boolean isAdmin) {
        if (isAdmin) {
            recruitNotAccepted(event);
        } else {
            ResponseMessage.noPermission(event);
        }
    }

    public void checkIsRecruit(String userId) {
        Optional<Recruit> recruitOptional = findByUserId(userId);
        if (recruitOptional.isEmpty()) {
            return;
        }
        Recruit recruit = recruitOptional.get();
        if (!recruit.getIsCloseChannel()) {
            Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
            Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            if (guild != null) {
                Member member = guild.getMemberById(recruit.getUserId());
                TextChannel channel = guild.getTextChannelById(recruit.getChannelId());
                if (member != null && channel != null) {
                    channel.getManager()
                            .putPermissionOverride(member, permissions, null)
                            .queue();
                }
            }
        }
    }

    public void confirmFormReceived(@NotNull ButtonInteractionEvent event) {
        boolean maxRecruits = isMaxRecruits();
        if (maxRecruits) {
            MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(messageEmbed);
            builder.setColor(new Color(255, 116, 0));
            List<Field> fields = builder.getFields();
            if (fields.size() >= 3) {
                fields.remove(2);
            }
            builder.addField("", "**Osiągnięto maksymalną liczbę kanałów rekrutacyjnych**", false);
            event.editMessageEmbeds(builder.build()).queue();
            return;
        }
        String userId = event.getComponentId().substring(CONFIRM_FORM_RECEIVED.length());
        String nickname = Users.getUserNicknameFromID(userId);
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder(messageEmbed);
        builder.setColor(Color.GREEN);
        List<Field> fields = builder.getFields();
        if (fields.size() >= 2) {
            fields.remove(1);
            if (fields.size() >= 3) {
                fields.remove(2);
            }
        }
        builder.addField("", "**POTWIERDZONO**", false);
        builder.setFooter("Podpis: " + Users.getUserNicknameFromID(event.getUser().getId()));
        event.editMessageEmbeds(builder.build()).setComponents().queue();
        createChannelForNewRecruit(nickname, userId);
        deleteByUserId(userId);
    }

    public void declineForm(@NotNull ButtonInteractionEvent event) {
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder(messageEmbed);
        builder.setColor(Color.RED);
        List<Field> fields = builder.getFields();
        if (fields.size() >= 2) {
            fields.remove(1);
            if (fields.size() >= 3) {
                fields.remove(2);
            }
        }
        builder.addField("", "**ODRZUCONO**", false);
        builder.setFooter("Podpis: " + Users.getUserNicknameFromID(event.getUser().getId()));
        event.editMessageEmbeds(builder.build()).setComponents().queue();
        String userId = event.getComponentId().substring(DECLINE_FORM_SEND.length());
        deleteByUserId(userId);
    }
}

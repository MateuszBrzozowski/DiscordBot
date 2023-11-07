package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.repository.main.RecruitBlackListRepository;
import pl.mbrzozowski.ranger.repository.main.RecruitRepository;
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

@Service
@Slf4j
public class RecruitsService {

    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_MENTION_EVERYONE);
    private final Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);
    private final RecruitRepository recruitRepository;
    private final RecruitBlackListRepository recruitBlackListRepository;
    private final int MAX_CHANNELS = 50;

    public RecruitsService(RecruitRepository recruitRepository, RecruitBlackListRepository recruitBlackListRepository) {
        this.recruitRepository = recruitRepository;
        this.recruitBlackListRepository = recruitBlackListRepository;
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
                .addRolePermissionOverride(Long.parseLong(RoleID.RADA_KLANU), permissions, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permViewChannel, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.DRILL_INSTRUCTOR_ID), permissions, null)
                .queue(textChannel -> {
                    sendWelcomeMessage(userID, textChannel);
                    add(userID, userName, textChannel.getId());
                });
    }

    public void sendWelcomeMessage(String userID, @NotNull TextChannel textChannel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.addField("Obowiązkowo:",
                """
                        **1. Uzupełnij formularz rekrutacyjny:**
                        https://forms.gle/fbTQSdxBVq3zU7FW9

                        **2. Przeczytaj manual - Najważniejsze zasady gry w Rangers Polska**
                        https://drive.google.com/file/d/18uefRZx5vIZrD-7wYQqgAk-JlYDgfQzq/view

                        **3. Jeżeli zaczynasz przygodę ze Squadem przeczytaj poradnik:**
                        https://steamcommunity.com/sharedfiles/filedetails/?id=2878029717""",
                false);
        builder.addField("", "", false);
        builder.addField("TeamSpeak3:", "ts.rangerspolska.pl:6969", false);
        builder.addField("", "Po wypełnieniu formularza skontaktuje się z Tobą <@&" + RoleID.DRILL_INSTRUCTOR_ID +
                "> w celu umówienia terminu rozmowy rekrutacyjnej.", false);
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
        log.info("New recruit application - " + event.getUser().getId());
        String userID = event.getUser().getId();
        if (Users.memberOnGuildShorterThan(userID, 10)) {
            ResponseMessage.noReqTimeOnServer(event);
            return;
        }
        if (hasRecruitChannel(userID)) {
            ResponseMessage.userHaveRecruitChannel(event);
            return;
        }
        if (isMaxRecruits()) {
            ResponseMessage.maxRecruits(event);
            return;
        }
        if (userBlackList(userID)) {
            ResponseMessage.userBlackList(event);
            return;
        }
        if (Users.hasUserRole(userID, RoleID.CLAN_MEMBER_ID)) {
            ResponseMessage.userIsInClanMember(event);
            return;
        }
        confirmMessage(event);
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
        event.reply("""
                        **Potwierdź czy chcesz złożyć podanie?**

                        Po potwierdzeniu poprosimy o wypełnienie krótkiego formularza.
                        Następnie skontaktuję się z Tobą jeden z naszych Drillów.""")
                .setEphemeral(true)
                .addActionRow(
                        Button.success(ComponentId.NEW_RECRUT_CONFIRM, "Potwierdzam")
                )
                .queue();
    }

    public void confirm(@NotNull ButtonInteractionEvent event) {
        log.info("Recruit confirm - " + event.getUser().getId());
        if (!hasRecruitChannel(event.getUser().getId())) {
            String userID = event.getUser().getId();
            String userName = Users.getUserNicknameFromID(userID);
            createChannelForNewRecruit(userName, userID);
            event.deferEdit().queue();
        } else {
            ResponseMessage.userHaveRecruitChannel(event);
        }
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

    private void add(String userId, String userName, String channelID) {
        log.info("Recruit adding to db.");
        Recruit recruit = Recruit.builder()
                .userId(userId)
                .name(userName)
                .channelId(channelID)
                .toApply(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime())
                .isCloseChannel(false)
                .build();
        log.info(recruit.toString());
        recruitRepository.save(recruit);
    }

    public void deleteChannelByID(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            removeRecruitRoleFromUserID(recruit.getUserId());
            recruitRepository.delete(recruit);
        }
    }

    private void addRoleRecruit(String userId) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Member member = guild.getMemberById(userId);
            Role roleRecruit = guild.getRoleById(RoleID.RECRUIT_ID);
            boolean hasRoleRecruit = Users.hasUserRole(userId, RoleID.RECRUIT_ID);
            if (!hasRoleRecruit && roleRecruit != null && member != null) {
                guild.addRoleToMember(member, roleRecruit).queue();
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
        log.info("Recruit accepted - " + event.getUser().getId());
        Optional<Recruit> recruitOptional = findByChannelId(event.getChannel().getId());
        if (recruitOptional.isEmpty()) {
            ResponseMessage.operationNotPossible(event);
            return;
        }
        Recruit recruit = recruitOptional.get();
        if (recruit.getStartRecruitment() != null) {
            ResponseMessage.recruitHasBeenAccepted(event);
            return;
        } else if (recruit.getRecruitmentResult() != null) {
            ResponseMessage.recruitHasBeenRejected(event);
            return;
        }
        addRoleRecruit(recruit.getUserId());
        addRecruitTag(event.getGuild(), recruit.getUserId());
        setYellowCircleInChannelName(event.getChannel().asTextChannel());
        EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getChannel().asTextChannel());
        recruit.setStartRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        recruitRepository.save(recruit);
        event.deferEdit().queue();
    }

    public boolean positiveResult(@NotNull ButtonInteractionEvent interactionEvent) {
        log.info("Recruit positive result - " + interactionEvent.getUser().getId());
        boolean result = positiveResult(interactionEvent.getUser().getId(), interactionEvent.getChannel().asTextChannel());
        if (result) {
            interactionEvent.deferEdit().queue();
        }
        return result;
    }

    public boolean positiveResult(String drillId, @NotNull TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
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

    public boolean negativeResult(@NotNull ButtonInteractionEvent interactionEvent) {
        log.info("Recruit negative result - " + interactionEvent.getUser().getId());
        boolean result = negativeResult(interactionEvent.getUser().getId(), interactionEvent.getChannel().asTextChannel());
        if (result) {
            interactionEvent.deferEdit().queue();
        }
        return result;
    }

    public boolean negativeResult(String drillId, @NotNull TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
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
                member.modifyNickname(nicknameOld + "<rRangersPL>").queue();
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
        log.info("Recruit close channel by " + event.getUser().getName());
        TextChannel textChannel = event.getChannel().asTextChannel();
        String userID = event.getUser().getId();
        recruitNotAccepted(event, textChannel, userID);
    }

    private void recruitNotAccepted(ButtonInteractionEvent event, TextChannel textChannel, String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Optional<Recruit> recruitOptional = findByChannelId(textChannel.getId());
            if (recruitOptional.isPresent()) {
                Recruit recruit = recruitOptional.get();
                if (recruit.getRecruitmentResult() != null) {
                    ResponseMessage.recruitHasBeenRejected(event);
                    return;
                } else if (recruit.getStartRecruitment() != null) {
                    ResponseMessage.recruitHasBeenAccepted(event);
                    return;
                }
                setRedCircleInChannelName(textChannel);
                event.deferEdit().queue();
                recruit.setIsCloseChannel(true);
                recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
                recruit.setRecruitmentResult(RecruitmentResult.NEGATIVE);
                recruitRepository.save(recruit);
                EmbedInfo.recruitNotAccepted(Users.getUserNicknameFromID(userID), textChannel);
            }
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

}

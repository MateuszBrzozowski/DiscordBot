package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.repository.main.RecruitBlackListRepository;
import pl.mbrzozowski.ranger.repository.main.RecruitRepository;
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

    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
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
                .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permViewChannel, null)
                .queue(textChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.GREEN);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setDescription("Obowiązkowo uzupełnij formularz oraz przeczytaj manual - pomoże Ci w ogarnięciu gry");
                    builder.addField("Formularz rekrutacyjny:", "https://forms.gle/fbTQSdxBVq3zU7FW9", false);
                    builder.addField("Manual:", "https://drive.google.com/file/d/1qTHVBEkpMUBUpTaIUR3TNGk9WAuZv8s8/view", false);
                    builder.addField("TeamSpeak3:", "daniolab.pl:6969", false);
                    textChannel.sendMessage("Cześć <@" + userID + ">!\n" +
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + RoleID.DRILL_INSTRUCTOR_ID + "> oraz innych członków klanu.\n" +
                                    "<@&" + RoleID.RADA_KLANU + "> ")
                            .setEmbeds(builder.build())
                            .queue();
                    textChannel.sendMessage("Wkrótce skontaktuje się z Tobą Drill. Oczekuj na wiadomość.")
                            .setActionRow(
                                    Button.primary(ComponentId.RECRUIT_IN, " "),
                                    Button.secondary(ComponentId.RECRUIT_CLOSE_CHANNEL, " "),
                                    Button.success(ComponentId.RECRUIT_POSITIVE, " "),
                                    Button.danger(ComponentId.RECRUIT_NEGATIVE, " "))
                            .queue();
                    add(userID, userName, textChannel.getId());
                });
    }

    public void newPodanie(@NotNull ButtonInteractionEvent event) {
        log.info("New recruit application - " + event.getUser().getId());
        String userID = event.getUser().getId();
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

                        Po potwierdzeniu rozpocznie się Twój okres rekrutacyjny w naszym klanie.
                        Poprosimy o wypełnienie krótkiego formularza.
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
            Role roleRecruit = guild.getRoleById(RoleID.RECRUT_ID);
            boolean hasRoleRecruit = Users.hasUserRole(userId, RoleID.RECRUT_ID);
            if (!hasRoleRecruit && roleRecruit != null && member != null) {
                guild.addRoleToMember(member, roleRecruit).queue();
            }
        }
    }

    public void removeRecruitRoleFromUserID(String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Member member = guild.getMemberById(userID);
            Role roleRecruit = guild.getRoleById(RoleID.RECRUT_ID);
            boolean hasRoleRecruit = Users.hasUserRole(userID, RoleID.RECRUT_ID);
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
        Optional<Recruit> recruitOptional = findByChannelId(event.getTextChannel().getId());
        if (recruitOptional.isEmpty()) {
            ResponseMessage.operationNotPossible(event);
            return;
        }
        Recruit recruit = recruitOptional.get();
        if (recruit.getStartRecruitment() != null || recruit.getRecruitmentResult() != null) {
            ResponseMessage.recruitHasBeenAccepted(event);
            return;
        }
        addRoleRecruit(recruit.getUserId());
        addRecruitTag(event.getGuild(), recruit.getUserId());
        EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getTextChannel());
        recruit.setStartRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        recruitRepository.save(recruit);
        event.deferEdit().queue();
    }

    public boolean positiveResult(@NotNull ButtonInteractionEvent interactionEvent) {
        log.info("Recruit positive result - " + interactionEvent.getUser().getId());
        boolean result = positiveResult(interactionEvent.getUser().getId(), interactionEvent.getTextChannel());
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
                    EmbedInfo.endPositive(drillId, recruit.getUserId(), channel);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean negativeResult(@NotNull ButtonInteractionEvent interactionEvent) {
        log.info("Recruit negative result - " + interactionEvent.getUser().getId());
        boolean result = negativeResult(interactionEvent.getUser().getId(), interactionEvent.getTextChannel());
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
                    EmbedInfo.endNegative(drillId, recruit.getUserId(), channel);
                    return true;
                }
            }
        }
        return false;
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


    public void closeChannel(@NotNull MessageReceivedEvent event) {
        log.info("Recruit close channel by " + event.getAuthor().getName());
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getAuthor().getId();
        closeChannel(textChannel, userID);
    }

    public void closeChannel(@NotNull ButtonInteractionEvent event) {
        log.info("Recruit close channel by " + event.getUser().getName());
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getUser().getId();
        closeChannel(textChannel, userID);
        event.deferEdit().queue();
    }

    private void closeChannel(TextChannel textChannel, String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Optional<Recruit> recruitOptional = findByChannelId(textChannel.getId());
            if (recruitOptional.isPresent()) {
                Recruit recruit = recruitOptional.get();
                Member member = guild.getMemberById(recruit.getUserId());
                TextChannelManager manager = textChannel.getManager();
                Role clanMemberRole = guild.getRoleById(RoleID.CLAN_MEMBER_ID);
                if (clanMemberRole != null && member != null) {
                    manager.putPermissionOverride(clanMemberRole, null, permViewChannel).queue();
                }
                if (member != null) {
                    manager.putPermissionOverride(member, null, permissions).queue();
                }
                recruit.setIsCloseChannel(true);
                recruitRepository.save(recruit);
                EmbedInfo.closeRecruitChannel(userID, textChannel);
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

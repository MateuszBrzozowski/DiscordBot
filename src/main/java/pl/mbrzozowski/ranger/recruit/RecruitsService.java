package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.helpers.*;
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
    private final int MAX_CHANNELS = 50;

    public RecruitsService(RecruitRepository recruitRepository) {
        this.recruitRepository = recruitRepository;
    }

    /**
     * @param userName Nazwa użytkownika
     * @param userID   ID użytkownika
     */
    public void createChannelForNewRecruit(String userName, String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            return;
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
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + "RoleID.DRILL_INSTRUCTOR_ID" + "> oraz innych członków klanu.\n" +
                                    "<@&" + "RoleID.RADA_KLANU" + "> ")
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
        log.info("Nowe podanie złożone.");
    }

    public void newPodanie(@NotNull ButtonInteractionEvent event) {
        String userID = event.getUser().getId();
        if (hasNotRecruitChannel(userID)) {
            if (!isMaxRecruits()) {
                if (!Users.hasUserRoleAnotherClan(event.getUser().getId())) {
                    if (!Users.hasUserRole(event.getUser().getId(), RoleID.CLAN_MEMBER_ID)) {
                        confirmMessage(event);
                    } else EmbedInfo.userIsInClanMember(event);
                } else EmbedInfo.userIsInClan(event);
            } else EmbedInfo.maxRecrutis(event);
        } else ResponseMessage.userHaveRecruitChannel(event);
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
        RangerLogger.info("Użytkownik [" + event.getUser().getName() + "] chce złożyć podanie.");
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
        if (hasNotRecruitChannel(event.getUser().getId())) {
            String userID = event.getUser().getId();
            String userName = Users.getUserNicknameFromID(userID);
            createChannelForNewRecruit(userName, userID);
            event.deferEdit().queue();
        }
    }

    private boolean hasNotRecruitChannel(String userId) {
        Optional<Recruit> recruitOptional = recruitRepository.findByUserIdAndNullChannelID(userId);
        return recruitOptional.isEmpty();
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
        Recruit recruit = Recruit.builder()
                .userId(userId)
                .name(userName)
                .channelId(channelID)
                .toApply(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime())
                .build();
        log.info(recruit.toString());
        recruitRepository.save(recruit);
    }

    public void deleteChannelByID(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getRecruitmentResult() == null) {
                recruit.setRecruitmentResult(RecruitmentResult.NEGATIVE);
            }
            if (recruit.getEndRecruitment() == null) {
                recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
            }
            recruit.setChannelId(null);
            recruitRepository.save(recruit);
            removeRoleFromUserID(recruit.getUserId());
        }
    }

    public void removeRoleFromUserID(String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            guild.retrieveMemberById(userID).queue(member -> {
                List<Role> roles = member.getRoles();
                for (Role r : roles) {
                    if (r.getId().equalsIgnoreCase(RoleID.RECRUT_ID)) {
                        member.getGuild().removeRoleFromMember(member, r).queue();
                        break;
                    }
                }
            });
        }
    }

    public boolean isRecruitChannel(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        return recruitOptional.isPresent();
    }

    public void deleteChannel(@NotNull Recruit recruit) {
        if (recruit.getChannelId() != null) {
            TextChannel textChannel = DiscordBot.getJda().getTextChannelById(recruit.getChannelId());
            if (textChannel != null) {
                textChannel.delete().reason("Rekrutacja zakończona.").queue();
                RangerLogger.info("Upłynął czas utrzymywania kanału - Usunięto pomyślnie kanał rekruta - [" + recruit.getName() + "]");
                recruit.setChannelId(null);
                recruitRepository.save(recruit);
            }
        }
    }

    public boolean positiveResult(String drillId, TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getEndRecruitment() == null &&
                    recruit.getRecruitmentResult() == null) {
                Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
                if (guild != null) {
                    removeSmallRInTag(recruit.getUserId(), guild);
                    Role roleClanMember = DiscordBot.getJda().getRoleById(RoleID.CLAN_MEMBER_ID);
                    Role roleRecruit = DiscordBot.getJda().getRoleById(RoleID.RECRUT_ID);
                    boolean hasRoleClanMember = Users.hasUserRole(recruit.getUserId(), RoleID.CLAN_MEMBER_ID);
                    boolean hasRoleRecruit = Users.hasUserRole(recruit.getUserId(), RoleID.RECRUT_ID);
                    if (!hasRoleClanMember && roleClanMember != null) {
                        guild.addRoleToMember(UserSnowflake.fromId(recruit.getUserId()), roleClanMember).submit();
                    }
                    if (hasRoleRecruit && roleRecruit != null) {
                        guild.removeRoleFromMember(UserSnowflake.fromId(recruit.getUserId()), roleRecruit).submit();
                    }
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

    public boolean negativeResult(String drillId, TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getEndRecruitment() == null && recruit.getRecruitmentResult() == null) {
                Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
                if (guild != null) {
                    removeTagFromNick(recruit.getUserId(), guild);
                    Role roleRecruit = DiscordBot.getJda().getRoleById(RoleID.RECRUT_ID);
                    boolean hasRoleRecruit = Users.hasUserRole(recruit.getUserId(), RoleID.RECRUT_ID);
                    if (hasRoleRecruit && roleRecruit != null) {
                        guild.removeRoleFromMember(UserSnowflake.fromId(recruit.getUserId()), roleRecruit).submit();
                    }
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
                member.modifyNickname(userNickname).submit();
            }
        }
    }

    private void removeTagFromNick(@NotNull String userId, @NotNull Guild guild) {
        String userNickname = Users.getUserNicknameFromID(userId);
        if (userNickname != null && userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "");
            Member member = guild.getMemberById(userId);
            if (member != null) {
                member.modifyNickname(userNickname).submit();
            }
        }
    }

    private void changeRecruitNickname(Guild guild, @NotNull String userId) {
        String nicknameOld = Users.getUserNicknameFromID(userId);
        if (!isNicknameRNGSuffix(nicknameOld)) {
            Member member = guild.getMemberById(userId);
            if (member != null) {
                member.modifyNickname(nicknameOld + "<rRangersPL>").complete();
            }
        }
    }

    protected boolean isNicknameRNGSuffix(String nickname) {
        nickname = nickname.replace(" ", "");
        if (nickname.endsWith("<rRangersPL>")) return true;
        else return nickname.endsWith("<RangersPL>");
    }

    public boolean accepted(ButtonInteractionEvent event) {
        Optional<Recruit> recruitOptional = findByChannelId(event.getTextChannel().getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getStartRecruitment() == null && recruit.getRecruitmentResult() == null) {
                EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getTextChannel());
                addRoleRecruit(recruit.getUserId());
                changeRecruitNickname(event.getGuild(), recruit.getUserId());
                recruit.setStartRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
                recruitRepository.save(recruit);
                return true;
            }
        }
        return false;
    }

    private void addRoleRecruit(String userId) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        Role roleRecruit = DiscordBot.getJda().getRoleById(RoleID.RECRUT_ID);
        boolean hasRoleRecruit = Users.hasUserRole(userId, RoleID.RECRUT_ID);
        if (!hasRoleRecruit && guild != null) {
            Member member = guild.getMemberById(userId);
            if (member != null && roleRecruit != null) {
                guild.addRoleToMember(member, roleRecruit).complete();
            }
        }
    }

    public void closeChannel(@NotNull MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getAuthor().getId();
        closeChannel(textChannel, userID);
    }

    public void closeChannel(@NotNull ButtonInteractionEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getUser().getId();
        closeChannel(textChannel, userID);
    }

    private void closeChannel(TextChannel textChannel, String userID) {
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Optional<Recruit> recruitOptional = findByChannelId(textChannel.getId());
            if (recruitOptional.isPresent()) {
                Member member = guild.getMemberById(recruitOptional.get().getUserId());
                TextChannelManager manager = textChannel.getManager();
                Role clanMemberRole = guild.getRoleById(RoleID.CLAN_MEMBER_ID);
                if (clanMemberRole != null && member != null) {
                    manager.putPermissionOverride(clanMemberRole, null, permViewChannel).queue();
                }
                if (member != null) {
                    manager.putPermissionOverride(member, null, permissions).queue();
                }
                EmbedInfo.closeChannel(userID, textChannel);
            }
        }
    }

    public void checkIsRecruit(String userId) {
        Optional<Recruit> recruitOptional = findByUserId(userId);
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getRecruitmentResult() == null) {
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
}

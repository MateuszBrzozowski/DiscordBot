package ranger.recruit;

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
import ranger.Repository;
import ranger.embed.EmbedInfo;
import ranger.helpers.*;
import ranger.model.MemberWithPrivateChannel;
import ranger.response.ResponseMessage;

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

    //    private final List<MemberWithPrivateChannel> activeRecruits = new ArrayList<>();
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
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
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
        log.info("Nowe podanie złożone.");
    }

    public void initialize() {
//        startUpList();
//        CleanerRecruitChannel cleaner = new CleanerRecruitChannel(activeRecruits);
//        cleaner.clean();
    }

    public void newPodanie(@NotNull ButtonInteractionEvent event) {
        String userID = event.getUser().getId();
        if (!userHasRecruitChannel(userID)) {
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
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
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

    public void confirm(ButtonInteractionEvent event) {
        String userID = event.getUser().getId();
        String userName = Users.getUserNicknameFromID(userID);

        if (!isActiveRecruit(userID)) {
            createChannelForNewRecruit(userName, userID);
            event.deferEdit().queue();
        } else {
            ResponseMessage.userHaveRecruitChannel(event);
        }
    }

    private boolean isActiveRecruit(String userId) {
        Optional<Recruit> recruitOptional = findByUserId(userId);
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            return recruit.getRecruitmentResult() == null;
        } else {
            return false;
        }
    }

    private Optional<Recruit> findByUserId(String userId) {
        return recruitRepository.findByUserId(userId);
    }

    private Optional<Recruit> findByChannelId(String channelId) {
        return recruitRepository.findByChannelId(channelId);
    }

    private void add(String userId, String userName, String channelID) {
        Recruit recruit = Recruit.builder()
                .userId(userId)
                .name(userName)
                .channelId(channelID)
                .toApply(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime())
                .build();
        recruitRepository.save(recruit);
    }

    public boolean userHasRecruitChannel(String userID) {
        return isActiveRecruit(userID);
    }


    public void deleteChannelByID(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getRecruitmentResult() == null) {
                recruit.setRecruitmentResult(RecruitmentResult.NEGATIVE);
            }
            if (recruit.getStartRecruitment() == null) {
                recruit.setStartRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
            }
            if (recruit.getEndRecruitment() == null) {
                recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
            }
            removeRoleFromUserID(recruit.getUserId());
            recruitRepository.save(recruit);
        }
    }

    public void removeRoleFromUserID(String userID) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
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

    public void closeChannel(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getAuthor().getId();
        closeChannel(textChannel, userID);
    }

    public boolean isRecruitChannel(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        return recruitOptional.isPresent();
    }

    public String getUserIdByChannelID(String channelID) {
        Optional<Recruit> recruitOptional = findByChannelId(channelID);
        return recruitOptional.map(Recruit::getUserId).orElse(null);
    }

    public String getChannelIdByUserId(String userID) {
        Optional<Recruit> recruitOptional = findByUserId(userID);
        return recruitOptional.map(Recruit::getChannelId).orElse(null);
    }

    public void deleteChannels(List<MemberWithPrivateChannel> listToDelete) {
        throw new Error("Method not implement");
//
//        JDA jda = Repository.getJda();
//        RecruitDatabase rdb = new RecruitDatabase();
//        for (int i = 0; i < listToDelete.size(); i++) {
//            int indexOfRecrut = getIndexOfRecruit(listToDelete.get(i).getChannelID());
//            String userName = listToDelete.get(i).getUserName();
//            activeRecruits.remove(indexOfRecrut);
//            rdb.removeUser(listToDelete.get(i).getChannelID());
//            logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
//            jda.getTextChannelById(listToDelete.get(i).getChannelID()).delete().reason("Rekrutacja zakończona, upłynął czas wyświetlania informacji").queue();
//            RangerLogger.info("Upłynął czas utrzymywania kanału - Usunięto pomyślnie kanał rekruta - [" + userName + "]");
//        }
    }

    public boolean positiveResult(TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getEndRecruitment() == null &&
                    recruit.getRecruitmentResult() == null) {
                Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
                if (guild == null) {
                    return false;
                }
                removeSmallRInTag(recruit.getUserId(), guild);
                Role roleClanMember = Repository.getJda().getRoleById(RoleID.CLAN_MEMBER_ID);
                Role roleRecruit = Repository.getJda().getRoleById(RoleID.RECRUT_ID);
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
                return true;
            }
        }
        return false;
    }

    /**
     * @param channel which happened event
     * @return true if result save correctly, else return false
     */
    public boolean negativeResult(TextChannel channel) {
        Optional<Recruit> recruitOptional = findByChannelId(channel.getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getEndRecruitment() == null && recruit.getRecruitmentResult() == null) {
                Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
                if (guild == null) {
                    return false;
                }
                removeTagFromNick(recruit.getUserId(), guild);
                Role roleRecruit = Repository.getJda().getRoleById(RoleID.RECRUT_ID);
                boolean hasRoleRecruit = Users.hasUserRole(recruit.getUserId(), RoleID.RECRUT_ID);
                if (hasRoleRecruit && roleRecruit != null) {
                    guild.removeRoleFromMember(UserSnowflake.fromId(recruit.getUserId()), roleRecruit).submit();
                }
                recruit.setEndRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
                recruit.setRecruitmentResult(RecruitmentResult.NEGATIVE);
                recruitRepository.save(recruit);
                return true;
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

    public void accepted(ButtonInteractionEvent event) {
        Optional<Recruit> recruitOptional = findByChannelId(event.getTextChannel().getId());
        if (recruitOptional.isPresent()) {
            Recruit recruit = recruitOptional.get();
            if (recruit.getStartRecruitment() == null) {
                EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getTextChannel());
                addRoleRecruit(recruit.getUserId());
                changeRecruitNickname(event.getGuild(), event.getTextChannel().getId());
                recruit.setStartRecruitment(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
                recruitRepository.save(recruit);
            }
        }
    }

    private void addRoleRecruit(String userId) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        Role roleRecruit = Repository.getJda().getRoleById(RoleID.RECRUT_ID);
        boolean hasRoleRecruit = Users.hasUserRole(userId, RoleID.RECRUT_ID);
        if (!hasRoleRecruit && guild != null) {
            Member member = guild.getMemberById(userId);
            if (member != null && roleRecruit != null) {
                guild.addRoleToMember(member, roleRecruit).complete();
            }
        }
    }

    public void closeChannel(ButtonInteractionEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getUser().getId();
        closeChannel(textChannel, userID);
    }

    private void closeChannel(TextChannel textChannel, String userID) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            return;
        }
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

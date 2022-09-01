package ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Service
@Slf4j
public class RecruitsService {

    //    private final List<MemberWithPrivateChannel> activeRecruits = new ArrayList<>();
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    private final Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);
    private final RecruitRepository recruitRepository;

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
                    addUserToList(userID, userName, textChannel.getId());
                });
        log.info("Nowe podanie złożone.");
    }

    public void initialize() {
        startUpList();
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
        } else EmbedInfo.userHaveRecrutChannel(event);
    }

    private boolean isMaxRecruits() {
        int MAX_CHANNELS = 50;
        int howManyChannelsNow = howManyChannelsInCategory(CategoryAndChannelID.CATEGORY_RECRUT_ID);
        return howManyChannelsNow >= MAX_CHANNELS;
    }

    private int howManyChannelsInCategory(String categoryID) {
        Guild guildRangersPL = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        return guildRangersPL.getCategoryById(categoryID).getChannels().size();
    }

    private void confirmMessage(@NotNull ButtonInteractionEvent event) {
        RangerLogger.info("Użytkownik [" + event.getUser().getName() + "] chce złożyć podanie.");
        event.reply("**Potwierdź czy chcesz złożyć podanie?**\n\n" +
                        "Po potwierdzeniu rozpocznie się Twój okres rekrutacyjny w naszym klanie. Poprosimy o wypełnienie krótkiego formularza. " +
                        "Następnie skontaktuję się z Tobą jeden z naszych Drillów.")
                .setEphemeral(true)
                .addActionRow(
                        Button.success(ComponentId.NEW_RECRUT_CONFIRM, "Potwierdzam")
                )
                .queue();
    }

    public void confirm(ButtonInteractionEvent event) {
        String userID = event.getUser().getId();
        String userName = Users.getUserNicknameFromID(userID);
        boolean isActiveRecruit = activeRecruits.stream().anyMatch(member -> member.getUserID().equalsIgnoreCase(userID));
        if (!isActiveRecruit) {
            createChannelForNewRecruit(userName, userID);
            event.deferEdit().queue();
        } else {
            EmbedInfo.userHaveRecrutChannel(event);
        }
    }

    private void addUserToList(String userID, String userName, String channelID) {
        MemberWithPrivateChannel member = new MemberWithPrivateChannel(userID, userName, channelID);
        activeRecruits.add(member);
        addUserToDataBase(userID, userName, channelID);
    }

    private void addUserToDataBase(String userID, String userName, String channelID) {
        RecruitDatabase rdb = new RecruitDatabase();
        rdb.addUser(userID, userName, channelID);
    }

    private void startUpList() {
        RecruitDatabase rdb = new RecruitDatabase();
        ResultSet resultSet = rdb.getAllRecrut();
        List<MemberWithPrivateChannel> recruitsToDeleteDataBase = new ArrayList<>();
        this.activeRecruits.clear();
        List<TextChannel> allTextChannels = Repository.getJda().getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        String channelID = resultSet.getString("channelID");
                        MemberWithPrivateChannel recrut = new MemberWithPrivateChannel(userID, userName, channelID);
                        boolean isActive = false;
                        for (TextChannel tc : allTextChannels) {
                            if (tc.getId().equalsIgnoreCase(channelID)) {
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive) {
                            activeRecruits.add(recrut);
                        } else {
                            recruitsToDeleteDataBase.add(recrut);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        for (MemberWithPrivateChannel rc : recruitsToDeleteDataBase) {
            RemoveRecrutFromDataBase(rc.getChannelID());
        }
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy
     * @return Zwraca true jeśli użytkownik ma otwarty kanał rekrutacji. W innym przypadku zwraca false.
     */
    public boolean userHasRecruitChannel(String userID) {
        for (MemberWithPrivateChannel member : activeRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }


    public void deleteChannelByID(String channelID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (channelID.equalsIgnoreCase(activeRecruits.get(i).getChannelID())) {
                removeRoleFromUserID(activeRecruits.get(i).getUserID());
                activeRecruits.remove(i);
                RemoveRecrutFromDataBase(channelID);
                logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            }
        }
    }

    public void removeRoleFromUserID(String userID) {
        JDA jda = Repository.getJda();
        jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID).retrieveMemberById(userID).queue(member -> {
            List<Role> roles = member.getRoles();
            for (Role r : roles) {
                if (r.getId().equalsIgnoreCase(RoleID.RECRUT_ID)) {
                    member.getGuild().removeRoleFromMember(member, r).queue();
                    break;
                }
            }
        });
    }

    private void RemoveRecrutFromDataBase(String channelID) {
        RecruitDatabase rdb = new RecruitDatabase();
        rdb.removeUser(channelID);
    }

    public void closeChannel(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getAuthor().getId();
        closeChannel(textChannel, userID);
    }

    private int getIndexOfRecrut(String channelID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(channelID)) {
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();
        boolean isRecruitChannel = isRecruitChannel(messageChannel.getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(messageChannel.getId());
            Member member = event.getGuild().getMemberById(activeRecruits.get(indexOfRecrut).getUserID());
            TextChannelManager manager = event.getTextChannel().getManager();
            manager.putPermissionOverride(event.getGuild().getRoleById(RoleID.CLAN_MEMBER_ID), permViewChannel, null);
            if (member != null)
                manager.putPermissionOverride(member, permissions, null);
            manager.queue();
            EmbedInfo.openChannel(event.getAuthor().getId(), event.getTextChannel());
        }
    }

    public boolean isRecruitChannel(String channelID) {
        for (MemberWithPrivateChannel ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return true;
            }
        }
        return false;
    }

    public String getRecruitIDFromChannelID(String channelID) {
        for (MemberWithPrivateChannel ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return ar.getUserID();
            }
        }
        return "-1";
    }

    public String getChannelIDFromRecruitID(String userID) {
        for (MemberWithPrivateChannel ar : activeRecruits) {
            if (ar.getUserID().equalsIgnoreCase(userID)) {
                return ar.getChannelID();
            }
        }
        return "-1";
    }

    public void deleteChannels(List<MemberWithPrivateChannel> listToDelete) {
        JDA jda = Repository.getJda();
        RecruitDatabase rdb = new RecruitDatabase();
        for (int i = 0; i < listToDelete.size(); i++) {
            int indexOfRecrut = getIndexOfRecrut(listToDelete.get(i).getChannelID());
            String userName = listToDelete.get(i).getUserName();
            activeRecruits.remove(indexOfRecrut);
            rdb.removeUser(listToDelete.get(i).getChannelID());
            logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            jda.getTextChannelById(listToDelete.get(i).getChannelID()).delete().reason("Rekrutacja zakończona, upłynął czas wyświetlania informacji").queue();
            RangerLogger.info("Upłynął czas utrzymywania kanału - Usunięto pomyślnie kanał rekruta - [" + userName + "]");
        }
    }

    public void sendInfo(PrivateChannel privateChannel) {
        EmbedBuilder activeRecruitsBuilder = new EmbedBuilder();
        activeRecruitsBuilder.setColor(Color.RED);
        activeRecruitsBuilder.setTitle("Rekruci");
        activeRecruitsBuilder.addField("Aktywnych rekrutacji", String.valueOf(activeRecruits.size()), false);
        privateChannel.sendMessageEmbeds(activeRecruitsBuilder.build()).queue();
        for (MemberWithPrivateChannel r : activeRecruits) {
            JDA jda = Repository.getJda();
            String channelName = jda.getTextChannelById(r.getChannelID()).getName();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.WHITE);
            builder.addField("ID użytkownika", r.getUserID(), false);
            builder.addField("Nazwa użytkownika", r.getUserName(), true);
            builder.addField("ID kanału", r.getChannelID(), false);
            builder.addField("Nazwa kanału", channelName, true);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        }
    }

    public void positiveResult(TextChannel channel) {
        if (isRecruitChannel(channel.getId())) {
            JDA jda = Repository.getJda();
            Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            removeSmallRInTag(channel.getId(), guild);
            Role roleClanMember = jda.getRoleById(RoleID.CLAN_MEMBER_ID);
            Role roleRecruit = jda.getRoleById(RoleID.RECRUT_ID);
            String recruitID = getRecruitIDFromChannelID(channel.getId());
            boolean hasRoleClanMember = Users.hasUserRole(recruitID, RoleID.CLAN_MEMBER_ID);
            boolean hasRoleRecrut = Users.hasUserRole(recruitID, RoleID.RECRUT_ID);
            if (!hasRoleClanMember) {
                guild.addRoleToMember(UserSnowflake.fromId(recruitID), roleClanMember).submit();
            }
            if (hasRoleRecrut) {
                guild.removeRoleFromMember(UserSnowflake.fromId(recruitID), roleRecruit).submit();
            }
        }
    }

    private void removeSmallRInTag(String channelId, Guild guild) {
        String userID = getRecruitIDFromChannelID(channelId);
        String userNickname = Users.getUserNicknameFromID(userID);
        if (userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "<RangersPL>");
            guild.getMemberById(userID).modifyNickname(userNickname).submit();
        }
    }

    public void negativeResult(TextChannel channel) {
        if (isRecruitChannel(channel.getId())) {
            JDA jda = Repository.getJda();
            Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            removeTagFromNick(channel.getId(), guild);
            Role roleRecrut = jda.getRoleById(RoleID.RECRUT_ID);
            String recruitID = getRecruitIDFromChannelID(channel.getId());
            boolean hasRoleRecrut = Users.hasUserRole(recruitID, RoleID.RECRUT_ID);
            if (hasRoleRecrut) {
                guild.removeRoleFromMember(UserSnowflake.fromId(recruitID), roleRecrut).submit();
            }
        }
    }

    private void removeTagFromNick(String channelID, Guild guild) {
        String userID = getRecruitIDFromChannelID(channelID);
        String userNickname = Users.getUserNicknameFromID(userID);
        if (userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "");
            guild.getMemberById(userID).modifyNickname(userNickname).submit();
        }
    }

    private void addRoleRecruit(String channelID) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        Role roleRecruit = jda.getRoleById(RoleID.RECRUT_ID);
        String userID = getRecruitIDFromChannelID(channelID);
        boolean hasRoleRocruit = Users.hasUserRole(userID, roleRecruit.getId());
        if (!hasRoleRocruit) {
            logger.info("daje role rekrut");
            Member member = guild.getMemberById(userID);
            guild.addRoleToMember(member, roleRecruit).complete();
        }
    }

    private void changeRecruitNickname(Guild guild, String channelID) {
        String userID = getRecruitIDFromChannelID(channelID);
        String nicknameOld = Users.getUserNicknameFromID(userID);
        if (!isNicknameRNGSuffix(nicknameOld)) {
            logger.info("Zmieniam nick");
            guild.getMemberById(userID).modifyNickname(nicknameOld + "<rRangersPL>").complete();
        }
    }

    protected boolean isNicknameRNGSuffix(String nickname) {
        nickname = nickname.replace(" ", "");
        if (nickname.endsWith("<rRangersPL>")) return true;
        else return nickname.endsWith("<RangersPL>");
    }

    public boolean isResult(TextChannel textChannel) {
        List<Message> messages = textChannel.getHistory().retrievePast(100).complete();
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (CleanerRecruitChannel.checkEmbeds(embeds)) {
                return true;
            }
        }
        return false;
    }

    public void accepted(ButtonInteractionEvent event) {
        if (!isAccepted(event.getTextChannel())) {
            if (isRecruitChannel(event.getChannel().getId())) {
                EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getTextChannel());
                addRoleRecruit(event.getTextChannel().getId());
                changeRecruitNickname(event.getGuild(), event.getTextChannel().getId());
            }
        }
    }

    private boolean isAccepted(TextChannel textChannel) {
        List<Message> messages = textChannel.getHistory().retrievePast(100).complete();
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (checkEmbedsIsAccepted(embeds)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEmbedsIsAccepted(List<MessageEmbed> embeds) {
        if (!embeds.isEmpty()) {
            return isEmbedIsAccepted(embeds.get(0));
        }
        return false;
    }

    private boolean isEmbedIsAccepted(MessageEmbed embed) {
        String title = embed.getTitle();
        String description = embed.getDescription();
        String pattern = "Przyjęty na rekrutację przez: ";
        if (title != null && title.equalsIgnoreCase("Przyjęty")) {
            if (description != null && description.length() >= pattern.length()) {
                description = description.substring(0, pattern.length());
                if (description.equalsIgnoreCase(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void closeChannel(ButtonInteractionEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getUser().getId();
        closeChannel(textChannel, userID);
    }

    private void closeChannel(TextChannel textChannel, String userID) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        boolean isRecruitChannel = isRecruitChannel(textChannel.getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(textChannel.getId());
            Member member = guild.getMemberById(activeRecruits.get(indexOfRecrut).getUserID());
            TextChannelManager manager = textChannel.getManager();
            manager.putPermissionOverride(guild.getRoleById(RoleID.CLAN_MEMBER_ID), null, permViewChannel);
            if (member != null)
                manager.putPermissionOverride(member, null, permissions);
            manager.queue();
            EmbedInfo.closeChannel(userID, textChannel);
        }
    }
}

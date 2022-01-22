package recrut;

import database.DBConnector;
import embed.EmbedInfo;
import embed.EmbedSettings;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.managers.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class Recruits {

    private List<Recrut> activeRecruits = new ArrayList<>();
    private List<Recrut> thinkingRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
    private Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);
    private final RangerLogger rangerLogger = new RangerLogger();

    /**
     * @param userName Nazwa użytkownika
     * @param userID   ID użytkownika
     */
    public void createChannelForNewRecrut(String userName, String userID) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        List<Category> categories = guild.getCategories();
        for (Category cat : categories) {
            if (cat.getId().equals(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                guild.createTextChannel("rekrut-" + userName, cat)
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
                                    .embed(builder.build())
                                    .queue();
                            textChannel.sendMessage("Wkrótce skontaktuje się z Tobą Drill. Oczekuj na wiadomość.").queue();
                            addUserToList(userID, userName, textChannel.getId());
                        });
            }
        }
        logger.info("Nowe podanie złożone.");
    }

    public void initialize(JDA jda) {
        startUpList(jda);
    }


    public void newPodanie(ButtonClickEvent event) {
        String userName = event.getUser().getName();
        String userID = event.getUser().getId();
        event.deferEdit().queue();
        if (!checkUser(userID)) {
            if (!checkThinkingUser(userID)) {
                if (!Users.hasUserRoleAnotherClan(event.getUser().getId())) {
                    if (!Users.hasUserRole(event.getUser().getId(), RoleID.CLAN_MEMBER_ID)) {
                        confirmMessage(userID, userName);
                    } else EmbedInfo.userIsInClanMember(userID);
                } else EmbedInfo.userIsInClan(userID);
            }
        } else EmbedInfo.userHaveRecrutChannel(userID);
    }

    private void confirmMessage(String userID, String userName) {
        rangerLogger.info("Użytkownik [" + userName + "] chce złożyć podanie.");
        thinkingRecruits.add(new Recrut(userID, userName));
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setTitle("Potwierdź czy chcesz złożyć podanie?");
            builder.setDescription("Po potwierdzeniu rozpocznie się Twój okres rekrutacyjny w naszym klanie. Skontaktuję się z Tobą jeden z naszych Drillów aby wprowadzić Cię" +
                    " w nasze szeregi. Poprosimy również o wypełnienie krótkiego formularza.");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            privateChannel.sendMessage(builder.build()).setActionRow(Button.success("recrutY", "Potwierdzam"), Button.danger("recrutN", "Rezygnuję")).queue(message -> {
                Thread timer = new Thread(() -> {
                    try {
                        Thread.sleep(1000 * 60 * 2); //2 minuty oczekiwania na odpowiedź
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (userIsThinking(userID) >= 0) {
                        sendCancelInfo(privateChannel);
                        cancel(userID, privateChannel, message.getId());
                        disableButtons(privateChannel, message.getId());
                    }
                });
                timer.start();
            });
        });
    }

    private void sendCancelInfo(PrivateChannel privateChannel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
        builder.setTitle("Uwaga");
        builder.setDescription("Brak odpowiedzi. Anuluje podanie.");
        privateChannel.sendMessage(builder.build()).queue();
    }

    public void confirm(String userID, MessageChannel privateChannel, String messageID) {
        int index = userIsThinking(userID);
        if (index >= 0) {
            createChannelForNewRecrut(thinkingRecruits.get(index).getUserName(), userID);
            thinkingRecruits.remove(index);
        } else {
            sendMessageBotReload(userID);
        }
        disableButtons(privateChannel, messageID);
    }

    public void cancel(String userID, MessageChannel privateChannel, String messageID) {
        int index = userIsThinking(userID);
        if (index >= 0) {
            rangerLogger.info("Użytkownik [" + thinkingRecruits.get(index).getUserName() + "] zrezygnował ze złożenia podania.");
            thinkingRecruits.remove(index);
        } else {
            sendMessageBotReload(userID);
        }
        disableButtons(privateChannel, messageID);
    }

    private void sendMessageBotReload(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("UPS! Coś poszło nie tak. Jeżeli chcesz złóż ponownie podanie.").queue();
        });
    }

    /**
     * @param userID ID użytkowanika który składa podanie
     * @return Zwraca index na liście thinkingRecruits, jeżeli użytkownik dostał wiadomość z prośbą o potwierdzenie
     * złożenie podania i dalej ma możliwość akceptacji; W innym przypadku zwraca -1
     */
    private int userIsThinking(String userID) {
        for (int i = 0; i < thinkingRecruits.size(); i++) {
            if (userID.equalsIgnoreCase(thinkingRecruits.get(i).getUserID())) {
                return i;
            }
        }
        return -1;
    }

    public void disableButtons(MessageChannel channel, String messageID) {
        channel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessage(messageEmbed).setActionRow(Button.success("recrutY", "Potwierdzam").asDisabled(), Button.danger("recrutN", "Rezygnuję").asDisabled()).queue();
        });
    }

    private void addUserToList(String userID, String userName, String channelID) {
        Recrut member = new Recrut(userID, userName, channelID);
        activeRecruits.add(member);
        addUserToDataBase(userID, userName, channelID);
    }

    private void addUserToDataBase(String userID, String userName, String channelID) {
        String query = "INSERT INTO `recruts` (`userID`, `userName`, `channelID`) VALUES (\"%s\",\"%s\",\"%s\")";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, userID, userName, channelID));
    }

    private void startUpList(JDA jda) {
        ResultSet resultSet = getAllRecrutFromDataBase();
        List<Recrut> recrutsToDeleteDataBase = new ArrayList<>();
        this.activeRecruits.clear();
        List<TextChannel> allTextChannels = jda.getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        String channelID = resultSet.getString("channelID");
                        Recrut recrut = new Recrut(userID, userName, channelID);
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
                            recrutsToDeleteDataBase.add(recrut);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        for (Recrut rc : recrutsToDeleteDataBase) {
            RemoveRecrutFromDataBase(rc.getChannelID());
        }
//        rangerLogger.Info(String.format("Aktywnych rekrutacji: %d",activeRecruits.size()));
        logger.info("Aktywnych rekrutacji: {}", activeRecruits.size());
    }

    private ResultSet getAllRecrutFromDataBase() {
        String query = "SELECT * FROM `recruts`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli recruts w bazie danych -> Tworze tabele.");
            String queryCreate = "CREATE TABLE recruts(" +
                    "userID VARCHAR(30) PRIMARY KEY," +
                    "userName VARCHAR(30) NOT NULL," +
                    "channelID VARCHAR(30) NOT NULL)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy
     * @return Zwraca true jeśli użytkownik ma otwarty kanał rekrutacji. W innym przypadku zwraca false.
     */
    private boolean checkUser(String userID) {
        for (Recrut member : activeRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy
     * @return Zwraca true jeśli użytkownik kliknął Złóż podanie i program cozekuje na odpowiedź. W innym przypoadku
     * zwraca false.
     */
    private boolean checkThinkingUser(String userID) {
        for (Recrut member : thinkingRecruits) {
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
        String query = "DELETE FROM `recruts` WHERE channelID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, channelID));
    }

    public void closeChannel(GuildMessageReceivedEvent event) {
        TextChannel textChannel = event.getChannel();
        boolean isRecruitChannel = isRecruitChannel(textChannel.getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(event.getChannel().getId());
            Member member = event.getGuild().getMemberById(activeRecruits.get(indexOfRecrut).getUserID());
            ChannelManager manager = textChannel.getManager();
            manager.putPermissionOverride(event.getGuild().getRoleById(RoleID.CLAN_MEMBER_ID), null, permViewChannel);
            if (member != null)
                manager.putPermissionOverride(member, null, permissions);
            manager.queue();
            EmbedInfo.closeChannel(event.getAuthor().getId(), event.getChannel());
        }

    }

    private int getIndexOfRecrut(String channelID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(channelID)) {
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(GuildMessageReceivedEvent event) {
        TextChannel textChannel = event.getChannel();
        boolean isRecruitChannel = isRecruitChannel(textChannel.getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(textChannel.getId());
            Member member = event.getGuild().getMemberById(activeRecruits.get(indexOfRecrut).getUserID());
            ChannelManager manager = textChannel.getManager();
            manager.putPermissionOverride(event.getGuild().getRoleById(RoleID.CLAN_MEMBER_ID), permViewChannel, null);
            if (member != null)
                manager.putPermissionOverride(member, permissions, null);
            manager.queue();
            EmbedInfo.openChannel(event.getAuthor().getId(), event.getChannel());
        }
    }

    public boolean isRecruitChannel(String channelID) {
        for (Recrut ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return true;
            }
        }
        return false;
    }

    public String getRecruitIDFromChannelID(String channelID) {
        for (Recrut ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return ar.getUserID();
            }
        }
        return "-1";
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        logger.info("Kanał jest kanałem rekrutacyjnym.");
        EmbedInfo.removedChannel(event.getChannel());
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deleteChannelByID(event.getChannel().getId());
            event.getGuild().getTextChannelById(event.getChannel().getId()).delete().reason("Rekrutacja zakończona").queue();
            logger.info("Kanał został usunięty.");
        });
        thread.start();
    }

    public void sendInfo(PrivateChannel privateChannel) {
        EmbedBuilder activeRecruitsBuilder = new EmbedBuilder();
        activeRecruitsBuilder.setColor(Color.RED);
        activeRecruitsBuilder.setTitle("Rekruci");
        activeRecruitsBuilder.addField("Aktywnych rekrutacji", String.valueOf(activeRecruits.size()), false);
        privateChannel.sendMessage(activeRecruitsBuilder.build()).queue();
        for (Recrut r : activeRecruits) {
            JDA jda = Repository.getJda();
            String channelName = jda.getTextChannelById(r.getChannelID()).getName();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.WHITE);
            builder.addField("ID użytkownika", r.getUserID(), false);
            builder.addField("Nazwa użytkownika", r.getUserName(), true);
            builder.addField("ID kanału", r.getChannelID(), false);
            builder.addField("Nazwa kanału", channelName, true);
            privateChannel.sendMessage(builder.build()).queue();
        }
    }

    public void positiveResult(TextChannel channel) {
        if (isRecruitChannel(channel.getId())) {
            //przypisanie roli ClanMember
            JDA jda = Repository.getJda();
            Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            Role roleClanMember = jda.getRoleById(RoleID.CLAN_MEMBER_ID);
            Role roleRecruit = jda.getRoleById(RoleID.RECRUT_ID);
            String recruitID = getRecruitIDFromChannelID(channel.getId());
            guild.addRoleToMember(recruitID, roleClanMember).queue();
            guild.removeRoleFromMember(recruitID,roleRecruit).queue();
            //Wysłanie informacji o przyjęciu nowego członka.
            TextChannel channelMemberOnly = jda.getTextChannelById(CategoryAndChannelID.CHANNEL_MEMBER_ONLY);
            channelMemberOnly.sendMessage("Witamy nowego członka klanu <@" + recruitID + ">.").queue();
        }
    }

    public void negativeResult(TextChannel channel) {
        if (isRecruitChannel(channel.getId())) {
            JDA jda = Repository.getJda();
            Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            Role roleRecrut = jda.getRoleById(RoleID.RECRUT_ID);
            String recruitID = getRecruitIDFromChannelID(channel.getId());
            guild.removeRoleFromMember(recruitID, roleRecrut).queue();
        }
    }
}

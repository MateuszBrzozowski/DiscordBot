package recrut;

import database.DBConnector;
import embed.*;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import helpers.RoleID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class Recruits {

    private List<Recrut> activeRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
    private Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);
    private final RangerLogger rangerLogger = new RangerLogger();

    public void createChannelForNewRecrut(ButtonClickEvent event, String userName, String userID) {
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equals(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                event.getGuild().createTextChannel("rekrut-" + userName, cat)
                        .addPermissionOverride(event.getGuild().getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permViewChannel, null)
                        .queue(textChannel -> {
                            textChannel.sendMessage("Cześć <@" + userID + ">!\n" +
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + RoleID.DRILL_INSTRUCTOR_ID + "> oraz innych członków klanu.\n" +
                                    "<@&" + RoleID.RADA_KLANU + "> ").queue();
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.GREEN);
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            builder.setDescription("Obowiązkowo uzupełnij formularz oraz przeczytaj manual - pomoże Ci w ogarnięciu gry");
                            builder.addField("Formularz rekrutacyjny:", "https://forms.gle/fbTQSdxBVq3zU7FW9", false);
                            builder.addField("Manual:", "https://drive.google.com/file/d/1qTHVBEkpMUBUpTaIUR3TNGk9WAuZv8s8/view", false);
                            builder.addField("TeamSpeak3:", "daniolab.pl:6969", false);
                            textChannel.sendMessage(builder.build()).queue();
                            textChannel.sendMessage("Wkrótce skontaktuje się z Tobą Drill. Oczekuj na wiadomość.").queue();
                            addUserToList(userID, userName, textChannel.getId());
                        });
            }
        }
        logger.info("Nowe podanie złożone. Aktywnych rekrutacji: {}", activeRecruits.size());
    }

    public void initialize(JDA jda) {
        startUpList(jda);
    }


    public void newPodanie(ButtonClickEvent event) {
        String userName = event.getUser().getName();
        String userID = event.getUser().getId();
        event.deferEdit().queue();
        if (!checkUser(userID)) {
            if (!RoleID.isRoleAnotherClanButtonClick(event)) {
                if (!RoleID.isRoleButtonClick(event, RoleID.CLAN_MEMBER_ID)) {
                    createChannelForNewRecrut(event, userName, userID);
                } else new EmbedYouAreClanMember(event);
            } else new EmbedYouAreInClan(event);
        } else new EmbedYouHaveRecrutChannel(event);
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

    private boolean checkUser(String userID) {
        for (Recrut member : activeRecruits) {
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
        JDA jda = RangerBot.getJda();
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
        boolean isRecruitChannel = isRecruitChannel(event.getChannel().getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member, null, permissions).queue();
                    new EmbedCloseChannel(event);
                    logger.info("Kanał zamkniety: {} , userName: {}, userID: {}", event.getChannel().getName(), user.getName(), user.getId());
                });
            });
        }

    }

    private int getIndexOfRecrut(GuildMessageReceivedEvent event) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(event.getChannel().getId())) {
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(GuildMessageReceivedEvent event) {
        boolean isRecruitChannel = isRecruitChannel(event.getChannel().getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member, permissions, null).queue();
                    new EmbedOpernChannel(event);
                    logger.info("Kanał otwarty: {} , userName: {}, userID: {}", event.getChannel().getName(), user.getName(), user.getId());
                });
            });
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

    public String getRecruitIDFromChannelID(GuildMessageReceivedEvent event) {
        for (Recrut ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())) {
                return ar.getUserID();
            }
        }
        return "-1";
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        logger.info("Kanał jest kanałem rekrutacyjnym.");
        new EmbedRemoveChannel(event);
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
}

package model;

import database.DBConnector;
import embed.EmbedCloseChannel;
import embed.EmbedOpernChannel;
import embed.EmbedRemoveChannel;
import helpers.IdRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class Recruits {

    private List<Recrut> activeRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Collection<Permission> permissions1 = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
    private Collection<Permission> permissionsTest = EnumSet.of(Permission.MESSAGE_WRITE);
    private final String CATEGORY_ID = "842886351346860112"; //Kategoria  Brzoza i Ranger testujo
    private final String PATH_ACTIVE_RECRUITS = "./src/main/resources/databaseFiles/ActiveRecruits.txt";
    private final String PATH_ACTIVE_RECRUITS_DIRECTORY = "./src/main/resources/databaseFiles";

    public void createChannelForNewRecrut(ButtonClickEvent event, String userName, String userID) {
        String idRadaKlanu = event.getGuild().getRolesByName("Rada Klanu", true).get(0).getId();
        String idDrill = event.getGuild().getRolesByName("Drill Instructor", true).get(0).getId();
        event.deferEdit().queue();
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equals(CATEGORY_ID)) {
                event.getGuild().createTextChannel("rekrut-" + userName, cat)
                        .addPermissionOverride(event.getGuild().getPublicRole(), null, permissions1)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions1, null)
//                        .setTopic(userID+";"+userName+";")
                        .queue(textChannel -> {
                            textChannel.sendMessage("Cześć <@" + userID + ">!\n" +
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + "Drill Instructor" + "> oraz innych członków klanu.\n" +
                                    "<@&" + "Rada Klanu" + "> ").queue();
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.GREEN);
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            builder.setDescription("Obowiązkowo uzupełnij formularz oraz przeczytaj manual - pomoże Ci w ogarnięciu gry");
                            builder.addField("Formularz rekrutacyjny:", "https://forms.gle/fbTQSdxBVq3zU7FW9", false);
                            builder.addField("Manual:", "https://drive.google.com/file/d/1qTHVBEkpMUBUpTaIUR3TNGk9WAuZv8s8/view", false);
                            builder.addField("TeamSpeak3:", "daniolab.pl:6969", false);
                            textChannel.sendMessage(builder.build()).queue();
                            textChannel.sendMessage("Wkrótce się z Tobą skontaktujemy.").queue();
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
        if (!checkUser(userID)) {
            createChannelForNewRecrut(event, userName, userID);
        } else {
            event.deferEdit().queue();
            event.getJDA().retrieveUserById(userID).queue(user -> {
                user.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("NIE MOŻESZ ZŁOŻYĆ WIĘCEJ NIŻ JEDNO PODANIE!");
                    builder.setColor(Color.red);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setDescription("Zlożyłeś już podanie do naszego klanu i\n" +
                            "jesteś w trakcie rekrutacji.\n");
                    builder.addField("Jeżeli masz pytania w związku z Twoją rekrutacją", "", false);
                    builder.addField("1. Spradź kanały", "Znajdź kanał przypisany do twojej rekrutacji i napisz do nas.", false);
                    builder.addField("2.Nie widze kanału.", "Jeżeli nie widzisz kanału przypisanego do twojej rekrutacji skontaktuj się z nami bezpośrednio. Drill Instrutor -> Rada Klanu.", false);
                    privateChannel.sendMessage(builder.build()).queue();
                });
            });
        }
    }

    private void addUserToList(String userID, String userName, String channelID) {
        Recrut member = new Recrut(userID, userName, channelID);
        activeRecruits.add(member);
        addUserToDataBase(userID,userName,channelID);
    }

    private void addUserToDataBase(String userID, String userName, String channelID) {
        String query = "INSERT INTO `recruts` (`userID`, `userName`, `channelID`) VALUES (\"%s\",\"%s\",\"%s\")";
        DBConnector connector = new DBConnector();
        logger.info(String.format(query,userID,userName,channelID));
        connector.executeQuery(String.format(query,userID,userName,channelID));
    }

    private void startUpList(JDA jda) {
        ResultSet resultSet = getAllRecrutFromDataBase();
        List<Recrut> recruts = new ArrayList<>();
        List<Recrut> recrutsToDeleteDataBase = new ArrayList<>();
        this.activeRecruits.clear();
        List<TextChannel> allTextChannels = jda.getTextChannels();

        if(resultSet!=null){
            while (true){
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        String channelID = resultSet.getString("channelID");
                        Recrut recrut = new Recrut(userID,userName,channelID);
                        boolean isActive =false;
                        for (TextChannel tc: allTextChannels){
                            if (tc.getId().equalsIgnoreCase(channelID)){
                                isActive=true;
                                break;
                            }
                        }
                        if (isActive){
                            activeRecruits.add(recrut);
                        }else {
                            recrutsToDeleteDataBase.add(recrut);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        for (Recrut rc: recrutsToDeleteDataBase){
            RemoveRecrutFromDataBase(rc.getChannelID());
        }
        logger.info("Aktywnych rekrutacji: {}", activeRecruits.size());
    }

    private ResultSet getAllRecrutFromDataBase() {
        String query = "SELECT * FROM `recruts`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        }catch (Exception e){
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
                activeRecruits.remove(i);
                RemoveRecrutFromDataBase(channelID);
                logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            }
        }

    }

    private void RemoveRecrutFromDataBase(String channelID) {
        String query = "DELETE FROM `recruts` WHERE channelID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,channelID));
    }

    public void closeChannel(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        boolean isRecruitChannel = isRecruitChannel(event);
        if (isRecruitChannel){
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member,null,permissions1).queue();
                    new EmbedCloseChannel(event);
                    logger.info("Kanał zamkniety: {} , userName: {}, userID: {}",event.getChannel().getName(),user.getName(),user.getId());
                });
            });
        }

    }

    private int getIndexOfRecrut(GuildMessageReceivedEvent event) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        boolean isRecruitChannel = isRecruitChannel(event);
        if (isRecruitChannel){
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member,permissions1,null).queue();
                    new EmbedOpernChannel(event);
                    logger.info("Kanał otwarty: {} , userName: {}, userID: {}",event.getChannel().getName(),user.getName(),user.getId());
                });
            });
        }
    }

    public boolean isRecruitChannel(GuildMessageReceivedEvent event) {
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isRecruitChannel(TextChannelUpdateTopicEvent event) {
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isRecruitChannel(TextChannelUpdateNameEvent event) {
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return true;
            }
        }
        return false;
    }

    public String getRecruitIDFromChannelID(GuildMessageReceivedEvent event){
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return ar.getUserID();
            }
        }
        return "-1";
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        if (isRecruitChannel(event)){
            logger.info("Kanał jest kanałem rekrutacyjnym.");
            event.getGuild().retrieveMemberById(event.getMessage().getAuthor().getId()).queue(member -> {
                List<Role> roles = member.getRoles();
                for (Role role : roles) {
                    if (role.getId().equalsIgnoreCase(IdRole.RADA_KLANU)) {
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
                        break;
                    }
                }
            });
        }else {
            logger.info("Kanał nie jest kanałem rekrutacyjnym.");
        }
    }
}

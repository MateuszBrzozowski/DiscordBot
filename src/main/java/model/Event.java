package model;

import database.DBConnector;
import embed.EmbedRemoveChannel;
import helpers.RoleID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private List<ActiveMatch> activeMatches = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String CATEGORY_ID = "842886351346860112"; //kategoria Brzoza i Ranger testujo
    private final String CLAN_MEMBER_ID = "311978154291888141";
    private static final String NAME_LIST = ":white_check_mark: Lista ";
    private static final String NAME_LIST_RESERVE = ":wc: Rezerwa ";


    public void initialize(JDA jda) {
        getAllDatabase(jda);

    }

    private void getAllDatabase(JDA jda) {
        DownladMatchesDB(jda);
        DownloadPlayersInMatechesDB();
    }

    private void DownladMatchesDB(JDA jda) {
        ResultSet resultSet = getAllMatches();
        List<ActiveMatch> matchesToDeleteDB = new ArrayList<>();
        this.activeMatches.clear();
        List<TextChannel> textChannels = jda.getTextChannels();

        if (resultSet!=null){
            while (true){
                try {
                    if (!resultSet.next()) break;
                    else {
                        String channelID = resultSet.getString("channelID");
                        String signINButtonID = resultSet.getString("signINButtonID");
                        String signINRButtonID = resultSet.getString("signINRButtonID");
                        String signOut = resultSet.getString("signOUTButtonID");
                        ActiveMatch match = new ActiveMatch(signINButtonID,signINRButtonID,signOut,channelID);
                        boolean isActive = false;
                        for (TextChannel tc : textChannels){
                            if (tc.getId().equalsIgnoreCase(channelID)){
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive){
                            activeMatches.add(match);
                        }else {
                            matchesToDeleteDB.add(match);
                        }
                    }
                }catch (SQLException throwables){
                    throwables.printStackTrace();
                }
            }
        }
        for (ActiveMatch a : matchesToDeleteDB){
            RemoveMemberFromEventDB(a.getChannelID());
            RemoveMatchDB(a.getChannelID());
        }
    }

    private void RemoveMatchDB(String channelID) {
        String query = "DELETE FROM `event` WHERE channelID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,channelID));
    }

    private void RemoveMemberFromEventDB(String channelID) {
        String query = "DELETE FROM `players` WHERE event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,channelID));
    }

    private ResultSet getAllMatches() {
        String query = "SELECT * FROM `event`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        }
        catch (Exception e){
            logger.info("Brak tabeli event w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE event(" +
                    "channelID VARCHAR(30) PRIMARY KEY," +
                    "signINButtonID VARCHAR(30) NOT NULL," +
                    "signINRButtonID VARCHAR(30) NOT NULL," +
                    "signOUTButtonID VARCHAR(30) NOT NULL)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    private void DownloadPlayersInMatechesDB() {
        ResultSet resultSet = getAllPlayers();
        if (resultSet!=null){
            while (true){
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        Boolean mainList = resultSet.getBoolean("mainList");
                        String event = resultSet.getString("event");
                        MemberMy memberMy = new MemberMy(userID,userName);
                        for (ActiveMatch m : activeMatches){
                            if (m.getChannelID().equalsIgnoreCase(event)){
                                if (mainList){
                                    m.addToMainList(memberMy);
                                }else {
                                    m.addToReserveList(memberMy);
                                }
                            }
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    private ResultSet getAllPlayers() {
        String query = "SELECT * FROM `players`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        }catch (Exception e){
            logger.info("Brak tabeli players w bazie danych -> Tworze tabele");
            String queryCreate = "CREATE TABLE players(" +
                    "id INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY, " +
                    "userID VARCHAR(30)," +
                    "userName VARCHAR(30) NOT NULL," +
                    "mainList BOOLEAN," +
                    "event VARCHAR(30) NOT NULL," +
                    "FOREIGN KEY (event) REFERENCES event(channelID))";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    public void createSignUpList3Data(String[] message, GuildMessageReceivedEvent event) {
        event.getMessage().delete().complete();
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CATEGORY_ID)) {
                event.getGuild().createTextChannel(message[1] + "-" + message[2] + "-" + message[3], cat).queue(textChannel -> {
                    textChannel.sendMessage("<@" + "Clan Member" + "> Zapisy!").queue();
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.setColor(Color.YELLOW);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setTitle(message[1]);
                    builder.addField(":date: Kiedy", message[2], true);
                    builder.addBlankField(true);
                    builder.addField(":clock930: Godzina", message[3], true);
                    builder.addBlankField(false);
                    builder.addField(NAME_LIST+"(0)", ">>> -", true);
                    builder.addBlankField(true);
                    builder.addField(NAME_LIST_RESERVE+"(0)", ">>> -", true);
                    builder.setFooter("Utworzony przez " + event.getMember().getNickname());
                    textChannel.sendMessage(builder.build()).setActionRow(
                                    Button.primary("in_"+textChannel.getId(), "Zapisz"),
                                    Button.secondary("reserve_"+textChannel.getId(), "Zapisz na rezerwę"),
                                    Button.danger("out_"+textChannel.getId(), "Wypisz")).queue();
                    ActiveMatch match = new ActiveMatch("in_"+textChannel.getId(),"reserve_"+textChannel.getId(),"out_"+textChannel.getId(),textChannel.getId());
                    activeMatches.add(match);
                    AddEventDB(match);
                });
                break;
            }
        }
    }

    private void AddEventDB(ActiveMatch match) {
        String query = "INSERT INTO `event` (`channelID` ,`signINButtonID`, `signINRButtonID`, `signOUTButtonID`) VALUES (\"%s\",\"%s\",\"%s\",\"%s\")";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,match.getChannelID(),match.getIdButtonSignUp(),match.getIdButtonSignUpReserve(),match.getIdButtonOut()));
    }

    public void updateEmbed(@NotNull ButtonClickEvent event, int indexOfMatch){
        String messageID = event.getMessage().getId();
        event.getChannel().retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed mOld = embeds.get(0);
            List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
            String mainList = activeMatches.get(indexOfMatch).getStringOfMainList();
            String reserveList = activeMatches.get(indexOfMatch).getStringOfReserveList();

            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i==4){
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(NAME_LIST+"("+ activeMatches.get(indexOfMatch).getMainList().size()+")",">>> "+mainList,true);
                    fieldsNew.add(fieldNew);
                }
                else if (i==6){
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(NAME_LIST_RESERVE + "(" + activeMatches.get(indexOfMatch).getReserveList().size() + ")", ">>> "+reserveList, true);
                    fieldsNew.add(fieldNew);
                }else {
                    fieldsNew.add(fieldsOld.get(i));
                }
            }

            int color;
            if (activeMatches.get(indexOfMatch).getMainList().size()>=9){
                color = Color.GREEN.getRGB();
            }else {
                color = Color.YELLOW.getRGB();
            }

            MessageEmbed m = new MessageEmbed(mOld.getUrl()
                    ,mOld.getTitle()
                    ,mOld.getDescription()
                    ,mOld.getType()
                    ,mOld.getTimestamp()
                    ,color
                    ,mOld.getThumbnail()
                    ,mOld.getSiteProvider()
                    ,mOld.getAuthor()
                    ,mOld.getVideoInfo()
                    ,mOld.getFooter()
                    ,mOld.getImage()
                    ,fieldsNew);
            message.editMessage(m).queue();

        });
    }

    public int isActiveMatch(String channelID){
        for (int i = 0; i < activeMatches.size(); i++) {
            if (channelID.equalsIgnoreCase(activeMatches.get(i).getChannelID())){
                return i;
            }
        }
        return -1;
    }

    public void signIn(ButtonClickEvent event, int indexOfActiveMatch) {
        String userName = event.getMember().getNickname();
        if (userName==null){
            userName = event.getUser().getName();
        }
        String userID = event.getUser().getId();
        activeMatches.get(indexOfActiveMatch).addToMainList(userID,userName,event);
    }

    public void signINReserve(ButtonClickEvent event, int indexOfActiveMatch) {
        String userName = event.getMember().getNickname();
        if (userName==null){
            userName = event.getUser().getName();
        }
        String userID = event.getUser().getId();
        activeMatches.get(indexOfActiveMatch).addToReserveList(userID,userName,event);
    }

    public void signOut(ButtonClickEvent event, int indexOfMatch) {
        String userID = event.getUser().getId();
        activeMatches.get(indexOfMatch).removeFromMatch(userID);
    }

    public void deleteChannelByID(String channelID) {
        int inexOfMatch = isActiveMatch(channelID);
        RemoveEventDB(channelID);
        activeMatches.remove(inexOfMatch);
    }

    private void RemoveEventDB(String channelID) {
        String queryPlayers = "DELETE FROM players WHERE event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(queryPlayers,channelID));

        String queryEvent = "DELETE FROM event WHERE channelID=\"%s\"";
        connector.executeQuery(String.format(queryEvent,channelID));
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        String channelID = event.getChannel().getId();
        if (isActiveMatch(channelID)>=0){
            logger.info("Kanal jest kanałem eventu");
            event.getGuild().retrieveMemberById(event.getMessage().getAuthor().getId()).queue(member -> {
                List<Role> roles = member.getRoles();
                for (Role r: roles){
                    if (r.getId().equalsIgnoreCase(RoleID.RADA_KLANU)){
                        new EmbedRemoveChannel(event);
                        Thread thread = new Thread(() -> {
                           try {
                               Thread.sleep(5000);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                           deleteChannelByID(channelID);
                           event.getGuild().getTextChannelById(channelID).delete().reason("Event zakończony").queue();
                           logger.info("Kanał {} usunięty", event.getChannel().getName());
                        });
                        thread.start();
                        break;
                    }
                }
            });
        }else {
            logger.info("Kanał nie jest kanałem eventowym.");
        }
    }
}

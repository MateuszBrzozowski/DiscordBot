package model;

import database.DBConnector;
import embed.EmbedRemoveChannel;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private List<ActiveMatch> activeMatches = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String CLAN_MEMBER_ID = "311978154291888141";
    private static final String NAME_LIST = ":white_check_mark: Lista ";
    private static final String NAME_LIST_RESERVE = ":wc: Rezerwa ";
    private RangerLogger rangerLogger = new RangerLogger();


    public void initialize(JDA jda) {
        getAllDatabase(jda);

    }

    private void getAllDatabase(JDA jda) {
        downladMatchesDB(jda);
        downloadPlayersInMatechesDB();
    }

    private void downladMatchesDB(JDA jda) {
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
            removeMemberFromEventDB(a.getChannelID());
            removeMatchDB(a.getChannelID());
        }
    }

    private void removeMatchDB(String channelID) {
        String query = "DELETE FROM `event` WHERE channelID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,channelID));
    }

    private void removeMemberFromEventDB(String channelID) {
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

    private void downloadPlayersInMatechesDB() {
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

    public void createNewEventFrom3Data(String[] message, GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        createEventChannel(event,message[1],message[2],message[3],null);
    }

    public void createNewEventFromSpecificData(String[] message, GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        String userName = event.getMessage().getMember().getNickname();
        if (userName==null){
            userName = event.getMessage().getAuthor().getName();
        }
//        rangerLogger.info(userName + " - tworzy nowy event.");
        if (checkMessage(message)){
            String nameEvent = getEventName(message);
            String date = getDate(message);
            String time = getTime(message);
            String description = getDescription(message);
            logger.info(nameEvent);
            logger.info(date);
            logger.info(time);
            logger.info(description);
            if (nameEvent!=null && date!=null && time!=null){
                createEventChannel(event,nameEvent,date,time,description);
            }else {
                logger.info("Nieprawidłowe dane w -name/-date/-time");
//                rangerLogger.info("Nieprawidłowe dane w -name/-date/-time");
            }
        }else {
            logger.info("Brak wymaganych parametrów -name <nazwa> -date <data> -time <czas>");
//            rangerLogger.info("Brak wymaganych parametrów -name <nazwa> -date <data> -time <czas>");
        }
    }

    private void createEventChannel(GuildMessageReceivedEvent event, String nameEvent, String date, String time, String description) {
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                event.getGuild().createTextChannel(nameEvent + "-" + date + "-" + time, cat).queue(textChannel -> {
                    textChannel.sendMessage("<@" + "Clan Member" + "> Zapisy!").queue();
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.YELLOW);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setTitle(nameEvent);
                    if (description!=null){
                        builder.setDescription(description);
                    }
                    builder.addField(":date: Kiedy", date, true);
                    builder.addBlankField(true);
                    builder.addField(":clock930: Godzina", time, true);
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
                    addEventDB(match);
                });
                break;
            }
        }
    }

    public String getDescription(String[] message) {
        int indexStart = getIndex(message,"-o");
        if (indexStart>0){
            int indexEnd = getIndexEnd(message,indexStart);
            if (indexStart>=indexEnd){
                return null;
            }else {
                String description = "";
                for (int i = indexStart+1; i <= indexEnd; i++) {
                    description+=message[i]+" ";
                }
                return description;
            }
        }
        return null;
    }

    public String getEventName(String[] message) {
        int indexStart = getIndex(message,"-name");
        int indexEnd = getIndexEnd(message,indexStart);
        if (indexStart>=indexEnd){
            return null;
        }else {
            String name = "";
            for (int i = indexStart+1; i <= indexEnd; i++) {
                name+=message[i]+" ";
            }
            return name;
        }
    }



    private String getDate(String[] message) {
        int indexStart = getIndex(message,"-date");
        if (!isEnd(message[indexStart+1])){
            return message[indexStart+1];
        }
        return null;
    }

    public boolean isDateFormat(String s,String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        df.setLenient(false);
        try {
            df.parse(s);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private String getTime(String[] message) {
        int indexStart = getIndex(message,"-time");
        if (!isEnd(message[indexStart+1])){
            return message[indexStart+1];
        }
        return null;
    }

    private int getIndexEnd(String[] message, int indexStart) {
        for (int i = indexStart+1; i < message.length; i++) {
            if (isEnd(message[i])){
                return i-1;
            }
        }
        return message.length-1;
    }

    private boolean isEnd(String s) {
        if (s.equalsIgnoreCase("-name")) return true;
        else if (s.equalsIgnoreCase("-date")) return true;
        else if (s.equalsIgnoreCase("-time")) return true;
        else if (s.equalsIgnoreCase("-o")) return true;
        else return false;
    }

    private int getIndex(String[] message, String s) {
        for (int i = 0; i < message.length; i++) {
            if (message[i].equalsIgnoreCase(s)){
                return i;
            }
        }
        return -1;
    }

    /**
     * @param message wpisana przez użytkownika
     *                Wiadomość musi posiadać 3 obowiazkowe parametry
     *                -name - nazwa eventu
     *                -data - data eventu
     *                -czas - kiedy odbywan sie event
     *                Moze i nie musi zawierac 4 parametru
     *                -opis - opis eventu
     * @return true - jeżeli zostały wpisane wszystkie 3 parametry; false - jeżeli parametry zostały nie zostały
     * wpisane prawidłowo
     */
    public boolean checkMessage(String[] message) {
        boolean name = false;
        boolean date = false;
        boolean time = false;
        for (String s : message){
            if (s.equalsIgnoreCase("-name")){
                name=true;
            }
            else if (s.equalsIgnoreCase("-date")){
                date = true;
            }
            else if (s.equalsIgnoreCase("-time")){
                time = true;
            }
        }
        if (name && date && time){
            return true;
        }
        else {
            return false;
        }

    }

    private void addEventDB(ActiveMatch match) {
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

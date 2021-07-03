package model;

import database.DBConnector;
import embed.EmbedRemoveChannel;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RangerLogger;
import helpers.RoleID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class Event {

    private List<ActiveMatch> activeMatches = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String NAME_LIST = ":white_check_mark: Lista ";
    private static final String NAME_LIST_RESERVE = ":regional_indicator_r: Niepewny/Rezerwa ";
    private Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
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
                        String messageID = resultSet.getString("msgID");
                        ActiveMatch match = new ActiveMatch(channelID,messageID);
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
            removeMemberFromEventDB(a.getMessageID());
            removeMatchDB(a.getMessageID());
        }
    }

    private void removeMatchDB(String messageID) {
        String query = "DELETE FROM `event` WHERE msgID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,messageID));
    }

    private void removeMemberFromEventDB(String messageID) {
        String query = "DELETE FROM `players` WHERE event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,messageID));
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
                    "msgID VARCHAR(30) PRIMARY KEY," +
                    "channelID VARCHAR(30) NOT NULL)";
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
                            if (m.getMessageID().equalsIgnoreCase(event)){
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
                    "FOREIGN KEY (event) REFERENCES event(msgID))";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    public void createNewEventFrom3Data(String[] message, GuildMessageReceivedEvent event) {
        createEventChannel(event,message[1],message[2],message[3],null,3);
    }

    public void createNewEventFrom4Data(String[] message, GuildMessageReceivedEvent event) {
        if (message[4].equalsIgnoreCase("-ac")){
            createEventChannel(event,message[1],message[2],message[3],null,1);
        }
        else if (message[4].equalsIgnoreCase("-r")){
            createEventChannel(event,message[1],message[2],message[3],null,2);
        }
        else {
            createEventChannel(event,message[1],message[2],message[3],null,3);
        }
    }

    public void createNewEventFrom3DataHere(String[] message, GuildMessageReceivedEvent event) {
        createList(getUserNameFromEvent(event),event.getChannel(),message[1],message[2],message[3],null,3);
    }

    public void createNewEventFrom4DataHere(String[] message, GuildMessageReceivedEvent event) {
        if (message[4].equalsIgnoreCase("-ac")){
            event.getChannel().getManager().putPermissionOverride(event.getGuild().getRoleById(RoleID.RECRUT_ID),permissions,null).queue();
            createList(getUserNameFromEvent(event),event.getChannel(),message[1],message[2],message[3],null,1);
        }
        else if (message[4].equalsIgnoreCase("-r")){
            event.getChannel().getManager().putPermissionOverride(event.getGuild().getRoleById(RoleID.RECRUT_ID),permissions,null).queue();
            createList(getUserNameFromEvent(event),event.getChannel(),message[1],message[2],message[3],null,2);
        }
        else {
            createList(getUserNameFromEvent(event),event.getChannel(),message[1],message[2],message[3],null,3);
        }

    }

    public void createNewEventFromSpecificData(String[] message, GuildMessageReceivedEvent event) {
        String userName = getUserNameFromEvent(event);
//        rangerLogger.info(userName + " - tworzy nowy event.");
        if (checkMessage(message)){
            String nameEvent = getEventName(message);
            String date = getDate(message);
            String time = getTime(message);
            String description = getDescription(message);
            boolean ac = searchParametrInMessage(message,"-ac");
            boolean r = searchParametrInMessage(message,"-r");
            boolean c = searchParametrInMessage(message,"-c");
            if (nameEvent!=null && date!=null && time!=null){
                if (message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)){
                    if (ac || r) event.getChannel().getManager().putPermissionOverride(event.getGuild().getRoleById(RoleID.RECRUT_ID),permissions,null).queue();
                    if (ac) createList(getUserNameFromEvent(event),event.getChannel(),nameEvent,date,time,description,1);
                    else if (r) createList(getUserNameFromEvent(event),event.getChannel(),nameEvent,date,time,description,2);
                    else if (c) createList(getUserNameFromEvent(event),event.getChannel(),nameEvent,date,time,description,3);
                    else createList(getUserNameFromEvent(event),event.getChannel(),nameEvent,date,time,description,-1);
                }else {
                    if (ac) createEventChannel(event,nameEvent,date,time,description,1);
                    else if (r) createEventChannel(event,nameEvent,date,time,description,2);
                    else createEventChannel(event,nameEvent,date,time,description,3);
                }
            }else {
                rangerLogger.info("Nieprawidłowe lub puste dane w obowiązkowych parametrach -name/-date/-time");
            }
        }else {
            rangerLogger.info("Brak wymaganych parametrów -name <nazwa> -date <data> -time <czas>");
        }
    }



    /**
     * @param event otrzymania wiadomości
     * @param nameEvent który tworzymy
     * @param date kiedy tworzymy event
     * @param time o której jest event
     * @param description eventu
     * @param whoVisable 1 - rekrut + clanMember; 2 - rekrut
     */
    private void createEventChannel(GuildMessageReceivedEvent event, String nameEvent, String date, String time, String description, int whoVisable) {
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_EVENT_ID)) {
                if (whoVisable==1 || whoVisable==2){
                    event.getGuild().createTextChannel(nameEvent + "-" + date + "-" + time, cat)
                            .addPermissionOverride(event.getGuild().getPublicRole(),null,permissions)
                            .addRolePermissionOverride(Long.parseLong(RoleID.RECRUT_ID),permissions,null)
                            .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID),permissions,null)
                            .queue(textChannel -> {
                                if (whoVisable==1) createList(getUserNameFromEvent(event),textChannel,nameEvent,date,time,description,1);
                                else if (whoVisable==2) createList(getUserNameFromEvent(event),textChannel,nameEvent,date,time,description,2);
                                else createList(getUserNameFromEvent(event),textChannel,nameEvent,date,time,description,3);

                    });
                }else {
                    event.getGuild().createTextChannel(nameEvent + "-" + date + "-" + time, cat).queue(textChannel -> {
                        createList(getUserNameFromEvent(event),textChannel,nameEvent,date,time,description,3);
                    });
                }

                break;
            }
        }
    }

    /**
     * @param userName który towrzy listę zapisów
     * @param textChannel kanał na którym jest tworzona lista
     * @param nameEvent który tworzymy
     * @param date kiedy tworzymy event
     * @param time o której jest event
     * @param description opis eventu
     * @param whoPing 1 - rekrut + clanMember; 2-rekrut; 3- tylko Clan Member
     */
    private void createList(String userName,TextChannel textChannel, String nameEvent, String date, String time, String description, int whoPing) {
        if (whoPing==1){
            textChannel.sendMessage("<@" + "Clan Member" + "> <@" + "RekrutID" + "> Zapisy!").queue();
        }
        else if(whoPing==2){
            textChannel.sendMessage("<@" + "RekrutID" + "> Zapisy!").queue();
        }
        else if (whoPing==3){
            textChannel.sendMessage("<@" + "Clan Member" + "> Zapisy!").queue();
        }
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
        builder.setFooter("Utworzony przez " + userName);
        textChannel.sendMessage(builder.build()).setActionRow(
                Button.primary("in_", "Zapisz"),
                Button.secondary("reserve_" ,"Niepewny"),
                Button.danger("out_", "Wypisz"))
                .queue(message -> {
                    MessageEmbed mOld = message.getEmbeds().get(0);
                    String msgID = message.getId();
                    message.editMessage(mOld).setActionRow(Button.primary("in_"+  msgID, "Zapisz"),
                            Button.secondary("reserve_"+msgID, "Zapisz na rezerwę"),
                            Button.danger("out_"+msgID, "Wypisz")).queue();
                    ActiveMatch match = new ActiveMatch(textChannel.getId(),msgID);
                    activeMatches.add(match);
                    addEventDB(match);
        });

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

    private boolean searchParametrInMessage(String[] message, String s) {
        for (int i = 1; i < message.length; i++) {
            if (message[i].equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
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
        else if (s.equalsIgnoreCase("-ac")) return true;
        else if (s.equalsIgnoreCase("-r")) return true;
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
        String query = "INSERT INTO `event` (`channelID`,`msgID`) VALUES (\"%s\",\"%s\")";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,match.getChannelID(),match.getMessageID()));
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

    public int isActiveMatch(String messageID){
        for (int i = 0; i < activeMatches.size(); i++) {
            if (messageID.equalsIgnoreCase(activeMatches.get(i).getMessageID())){
                return i;
            }
        }
        return -1;
    }

    public int isActiveMatchChannelID(String channelID){
        for (int i = 0; i < activeMatches.size(); i++) {
            if (channelID.equalsIgnoreCase(activeMatches.get(i).getChannelID())){
                return i;
            }
        }
        return -1;
    }

    public void signIn(ButtonClickEvent event, int indexOfActiveMatch) {
        String userName = getUserNameFromEvent(event);
        String userID = event.getUser().getId();
        activeMatches.get(indexOfActiveMatch).addToMainList(userID,userName,event);
    }

    public void signINReserve(ButtonClickEvent event, int indexOfActiveMatch) {
        String userName = getUserNameFromEvent(event);
        String userID = event.getUser().getId();
        activeMatches.get(indexOfActiveMatch).addToReserveList(userID,userName,event);
    }

    public void signOut(ButtonClickEvent event, int indexOfMatch) {
        String userID = event.getUser().getId();
        activeMatches.get(indexOfMatch).removeFromMatch(userID);
    }

    public void deleteChannelByID(String channelID) {
        while (true){
            int inexOfMatch = isActiveMatchChannelID(channelID);
            if (inexOfMatch==-1){
                break;
            }
            RemoveEventDB(activeMatches.get(inexOfMatch).getMessageID());
            activeMatches.remove(inexOfMatch);
        }
    }

    public void RemoveEvent(String messageID){
        for (int i = 0; i < activeMatches.size(); i++) {
            if (activeMatches.get(i).getMessageID().equalsIgnoreCase(messageID)){
                RemoveEventDB(messageID);
                activeMatches.remove(i);
                break;
            }
        }
    }

    private void RemoveEventDB(String messageID) {
        String queryPlayers = "DELETE FROM players WHERE event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(queryPlayers,messageID));

        String queryEvent = "DELETE FROM event WHERE msgID=\"%s\"";
        connector.executeQuery(String.format(queryEvent,messageID));
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        logger.info("Na kanale znajdują się listy/zapisy na eventy");
        new EmbedRemoveChannel(event);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deleteChannelByID(event.getChannel().getId());
            event.getGuild().getTextChannelById(event.getChannel().getId()).delete().reason("Event zakończony").queue();
            logger.info("Kanał {} usunięty przez {}", event.getChannel().getName(), event.getAuthor().getName());
        });
        thread.start();
    }

    private String getUserNameFromEvent(GuildMessageReceivedEvent event) {
        String userName = event.getMessage().getMember().getNickname();
        if (userName==null){
            userName = event.getMessage().getAuthor().getName();
        }
        return userName;
    }

    private String getUserNameFromEvent(ButtonClickEvent event) {
        String userName = event.getMember().getNickname();
        if (userName==null){
            userName = event.getUser().getName();
        }
        return userName;
    }

}

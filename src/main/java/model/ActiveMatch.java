package model;

import database.DBConnector;
import embed.EmbedCantSignOut;
import embed.EmbedCantSignInReserve;
import embed.EmbedCantSignIn;
import helpers.RangerLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.ArrayList;
import java.util.List;

public class ActiveMatch {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private String channelID;
    private String messageID; //message with embed List
    private List<MemberMy> mainList = new ArrayList<>();
    private List<MemberMy> reserveList = new ArrayList<>();
    private RangerLogger rangerLogger = new RangerLogger();

    /**
     * @param channelID ID kanału na którym jest lista
     * @param messageID ID wiadomości na której jest embed z Lista.
     */
    public ActiveMatch(String channelID, String messageID) {
        this.channelID = channelID;
        this.messageID = messageID;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getMessageID() {
        return messageID;
    }

    public List<MemberMy> getMainList() {
        return mainList;
    }

    public List<MemberMy> getAllPlayersInEvent(){
        List<MemberMy> all  = getMainList();
        List<MemberMy> reserve  = getReserveList();
        for (MemberMy m : reserve){
            all.add(m);
        }
        return all;
    }

    public List<MemberMy> getReserveList() {
        return reserveList;
    }

    public void addToMainList(MemberMy member, ButtonClickEvent event){
        if (checkMemberOnMainList(member)){
            new EmbedCantSignIn(event,member.getUserID());
        }else {
            removeFromReserveList(member.getUserID());
            mainList.add(member);
            AddPlayerDB(member,true);
            rangerLogger.info(member.getUserName() + " zapisał się na listę.",event.getChannel().getName());
            logger.info("Dodano do listy głównej.");
        }
    }

    public void addToMainList(String userID,String userName,ButtonClickEvent event){
        MemberMy memberMy = new MemberMy(userID,userName);
        addToMainList(memberMy,event);
    }

    public void addToReserveList(MemberMy member, ButtonClickEvent event){
        if (checkMemberOnReserveList(member)){
            new EmbedCantSignInReserve(event,member.getUserID());
        }else {
            removeFromMainList(member.getUserID());
            reserveList.add(member);
            AddPlayerDB(member,false);
            rangerLogger.info(member.getUserName() + " zapisał się na listę rezerwową.",event.getChannel().getName());
            logger.info("Dodano do listy rezerwowej.");
        }
    }

    public void addToReserveList(String userID,String userName, ButtonClickEvent event){
        MemberMy memberMy = new MemberMy(userID,userName);
        addToReserveList(memberMy,event);
    }

    public void removeFromMainList(String userID){
        if (checkMemberOnMainList(userID)){
            for (int i = 0; i < mainList.size(); i++) {
                if (mainList.get(i).getUserID().equalsIgnoreCase(userID)){
                    mainList.remove(i);
                    RemovePlayerDB(userID);
                    logger.info("Usunieto z listy głównej");
                }
            }
        }
    }

    public void removeFromReserveList(String userID){
        if (checkMemberOnReserveList(userID)){
            for (int i = 0; i < reserveList.size(); i++) {
                if (reserveList.get(i).getUserID().equalsIgnoreCase(userID)){
                    reserveList.remove(i);
                    RemovePlayerDB(userID);
                    logger.info("Usunieto z listy rezerwowej");
                }
            }
        }
    }

    private boolean checkMemberOnReserveList(MemberMy member) {
        for (int i = 0; i < reserveList.size(); i++) {
            if (reserveList.get(i).getUserID().equalsIgnoreCase(member.getUserID())){
                return true;
            }
        }
        return false;
    }

    private void AddPlayerDB(MemberMy member, boolean b) {
        String query = "INSERT INTO players (`userID`, `userName`, `mainList`, `event`) VALUES (\"%s\", \"%s\", %b, \"%s\")";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, member.getUserID(),member.getUserName(),b,messageID));
    }

    private void RemovePlayerDB(String userID) {
        String query = "DELETE FROM players WHERE userID=\"%s\" AND event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,userID,messageID));
    }

    private boolean checkMemberOnMainList(MemberMy member) {
        for (int i = 0; i < mainList.size(); i++) {
            if (mainList.get(i).getUserID().equalsIgnoreCase(member.getUserID())){
                return true;
            }
        }
        return false;
    }

    private boolean checkMemberOnReserveList(String userID) {
        for (int i = 0; i < reserveList.size(); i++) {
            if (reserveList.get(i).getUserID().equalsIgnoreCase(userID)){
                return true;
            }
        }
        return false;
    }

    private boolean checkMemberOnMainList(String userID) {
        for (int i = 0; i < mainList.size(); i++) {
            if (mainList.get(i).getUserID().equalsIgnoreCase(userID)){
                return true;
            }
        }
        return false;
    }


    public void removeFromMatch(String userID) {
        String userName = SearchAndGetUserName(userID);
        removeFromMainList(userID);
        removeFromReserveList(userID);
        if (userName!=null){
            rangerLogger.info(userName + " wypisał się z listy", getChannelName(channelID));
        }else {
            new EmbedCantSignOut(userID);
        }

    }

    private String getChannelName(String channelID) {
        JDA jda = RangerBot.getJda();
        List<TextChannel> textChannels = jda.getTextChannels();
        for (TextChannel t: textChannels){
            if (t.getId().equalsIgnoreCase(channelID)){
                return t.getName();
            }
        }
        return null;
    }

    private String SearchAndGetUserName(String userID) {
        for (MemberMy m : mainList){
            if (m.getUserID().equalsIgnoreCase(userID)){
                return m.getUserName();
            }
        }
        for (MemberMy m : reserveList){
            if (m.getUserID().equalsIgnoreCase(userID)){
                return m.getUserName();
            }
        }
        return null;
    }

    public String getStringOfMainList() {
        if (mainList.size()>0){
            String result = "";
            for (int i = 0; i < mainList.size(); i++) {
                result+=mainList.get(i).getUserName()+"\n";
            }
            return result;
        }else {
            return "-";
        }
    }

    public String getStringOfReserveList() {
        if (reserveList.size()>0){
            String result = "";
            for (int i = 0; i < reserveList.size(); i++) {
                result+=reserveList.get(i).getUserName()+"\n";
            }
            return result;
        }else {
            return "-";
        }
    }

    public void addToMainList(MemberMy memberMy) {
        mainList.add(memberMy);
    }

    public void addToReserveList(MemberMy memberMy) {
        reserveList.add(memberMy);
    }
}

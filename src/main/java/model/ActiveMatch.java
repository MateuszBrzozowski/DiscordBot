package model;

import database.DBConnector;
import embed.EmbedCantSignToReserve;
import embed.EmbedCantSignUp;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.ArrayList;
import java.util.List;

public class ActiveMatch {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private String idButtonSignUp;
    private String idButtonSignUpReserve;
    private String idButtonOut;
    private String channelID;
    private List<MemberMy> mainList = new ArrayList<>();
    private List<MemberMy> reserveList = new ArrayList<>();

    public ActiveMatch(String idButtonSignUp,String idButtonSignUpReserve, String idButtonOut, String channelID) {
        this.channelID = channelID;
        this.idButtonSignUp = idButtonSignUp;
        this.idButtonSignUpReserve = idButtonSignUpReserve;
        this.idButtonOut = idButtonOut;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
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
            new EmbedCantSignUp(event,member.getUserID());
        }else {
            removeFromReserveList(member.getUserID());
            mainList.add(member);
            AddPlayerDB(member,true);
            logger.info("Dodano do listy głównej.");
        }
    }

    public void addToMainList(String userID,String userName,ButtonClickEvent event){
        MemberMy memberMy = new MemberMy(userID,userName);
        addToMainList(memberMy,event);
    }

    public void addToReserveList(MemberMy member, ButtonClickEvent event){
        if (checkMemberOnReserveList(member)){
            new EmbedCantSignToReserve(event,member.getUserID());
        }else {
            removeFromMainList(member.getUserID());
            reserveList.add(member);
            AddPlayerDB(member,false);
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
        connector.executeQuery(String.format(query, member.getUserID(),member.getUserName(),b,channelID));
    }

    private void RemovePlayerDB(String userID) {
        String query = "DELETE FROM players WHERE userID=\"%s\" AND event=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query,userID,channelID));
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
        removeFromMainList(userID);
        removeFromReserveList(userID);
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

    public String getIdButtonSignUp() {
        return idButtonSignUp;
    }

    public String getIdButtonSignUpReserve() {
        return idButtonSignUpReserve;
    }

    public String getIdButtonOut() {
        return idButtonOut;
    }
}

package model;

import java.util.ArrayList;
import java.util.List;

public class ActiveMatch {

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

    public List<MemberMy> getReserveList() {
        return reserveList;
    }

    public void addToMainList(MemberMy member){
        if (checkMemberOnMainList(member)){
            //TODO wiadomosc prywatna - jestes juz na liscie, nie ma? to kontakt.
        }else {
            if (checkMemberOnReserveList(member)){
                //TODO usunac z listy reservy
            }
            mainList.add(member);
        }
    }

    public void addToMainList(String userID,String userName){
        MemberMy memberMy = new MemberMy(userID,userName);
        addToMainList(memberMy);
    }

    public void addToReserveList(MemberMy member){
        if (checkMemberOnReserveList(member)){
            //TODO wiadomosc prywatna - jestes juz na liscie, nie ma? to kontakt.
        }else {
            if (checkMemberOnMainList(member)){
                //TODO usunac z listy main
            }
            reserveList.add(member);
        }
    }

    public void addToReserveList(String userID,String userName){
        MemberMy memberMy = new MemberMy(userID,userName);
        addToReserveList(memberMy);
    }

    public void removeFromMainList(String userID){
        if (checkMemberOnMainList(userID)){
            //TODO usuwamy z listy
        }

    }

    public void removeFromReserveList(String userID){
        if (checkMemberOnReserveList(userID)){
            //TODO usuwamy z listy rezerwy
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


}

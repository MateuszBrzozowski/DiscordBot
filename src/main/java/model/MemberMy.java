package model;

public class MemberMy {

    private String userID;
    private String userName;

    public MemberMy(String userID, String userName) {
        this.userName = userName;
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserNameWithoutRangers() {
        String result = userName;
        if (result.matches("(.*)<rRangersPL>(.*)")){
            result = result.replace("<rRangersPL>","");
        }
        else if (result.matches("(.*)<RangersPL>(.*)")){
            result = result.replace("<RangersPL>","");
        }
        return result;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}

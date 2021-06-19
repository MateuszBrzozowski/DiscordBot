package model;

public class ActiveRecruits {

    private String userName;
    private String userID;
    private String channelID;

    public ActiveRecruits(String userID, String userName, String channelID) {
        this.userName = userName;
        this.userID = userID;
        this.channelID = channelID;
    }

    public String getUserName() {
        return userName;
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

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }
}

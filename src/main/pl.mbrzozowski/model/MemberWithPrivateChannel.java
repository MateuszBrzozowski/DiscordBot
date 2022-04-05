package model;

public class MemberWithPrivateChannel extends MemberOfServer {

    private String channelID;

    public MemberWithPrivateChannel(String userID, String userName, String channelID) {
        super(userID, userName);
        this.channelID = channelID;
    }

    public MemberWithPrivateChannel(String userID, String userName) {
        super(userID, userName);
    }

    public String getChannelID() {
        return channelID;
    }
}

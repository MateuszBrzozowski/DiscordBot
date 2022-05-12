package model;

public class MemberWithPrivateChannel extends MemberOfServer {

    private final String channelID;

    public MemberWithPrivateChannel(String userID, String userName, String channelID) {
        super(userID, userName);
        this.channelID = channelID;
    }

    public String getChannelID() {
        return channelID;
    }
}

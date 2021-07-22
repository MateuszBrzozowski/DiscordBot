package recrut;

public class Recrut {

    private String userName;
    private String userID;
    private String channelID;
    private String msgIDOnPrivateChannel;

    public Recrut(String userID, String userName, String channelID) {
        this.userName = userName;
        this.userID = userID;
        this.channelID = channelID;
    }

    public Recrut(String userID, String userName) {
        this.userID = userID;
        this.userName = userName;
    }

    public Recrut(RecrutBuilder recrutBuilder) {
        this.userID = recrutBuilder.userID;
        this.userName = recrutBuilder.userName;
        this.channelID = recrutBuilder.channelID;
        this.msgIDOnPrivateChannel = recrutBuilder.msgIDOnPrivateChannel;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserID() {
        return userID;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getMsgIDOnPrivateChannel() {
        return msgIDOnPrivateChannel;
    }

    public static class RecrutBuilder {
        private String userName;
        private String userID;
        private String channelID;
        private String msgIDOnPrivateChannel;

        public RecrutBuilder buildUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public RecrutBuilder buildUserID(String userID) {
            this.userID = userID;
            return this;
        }

        public RecrutBuilder buildChannelID(String channelID) {
            this.channelID = channelID;
            return this;
        }

        public RecrutBuilder buildMsgIDOnPrivateChannel(String msgID) {
            this.msgIDOnPrivateChannel = msgID;
            return this;
        }

        public Recrut build() {
            return new Recrut(this);
        }

    }
}

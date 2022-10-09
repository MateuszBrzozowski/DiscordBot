package pl.mbrzozowski.ranger.model;

public class MemberOfServer {

    private final String userID;
    private final String userName;

    public MemberOfServer(String userID, String userName) {
        this.userName = userName;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }
}

package model;

public class MemberOfServer {

    private final String userID;
    private final String userName;

    public MemberOfServer(String userID, String userName) {
        this.userName = userName;
        this.userID = userID;
    }

    public String getUserNameWithoutRangers() {
        String result = userName;
        if (result.matches("(.*)<rRangersPL>(.*)")) {
            result = result.replace("<rRangersPL>", "");
        } else if (result.matches("(.*)<RangersPL>(.*)")) {
            result = result.replace("<RangersPL>", "");
        }
        return result;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }
}

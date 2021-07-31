package bot.event.writing;

import helpers.RoleID;
import helpers.Users;

public class Message {

    private String[] words;
    private String contentDisplay;
    private boolean admin;
    private boolean clanMember;

    public Message(String[] words, String contentDisplay, String userID) {
        this.words = words;
        this.contentDisplay = contentDisplay;
        this.admin = Users.hasUserRole(userID, RoleID.RADA_KLANU);
        if (!this.admin) Users.isUserDev(userID);
        this.clanMember = Users.hasUserRole(userID, RoleID.CLAN_MEMBER_ID);
    }

    public String[] getWords() {
        return words;
    }

    public String getContentDisplay() {
        return contentDisplay;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isClanMember() {
        return clanMember;
    }
}

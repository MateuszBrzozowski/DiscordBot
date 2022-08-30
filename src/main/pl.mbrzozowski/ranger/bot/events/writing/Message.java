package ranger.bot.events.writing;

import ranger.helpers.RoleID;
import ranger.helpers.Users;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Message {

    private final String[] words;
    private final String contentDisplay;
    private final boolean admin;
    private final boolean clanMember;
    private final boolean isPrivate;
    private final String userID;

    public Message(String[] words, String contentDisplay, String userID, @NotNull MessageReceivedEvent event) {
        this.words = words;
        this.contentDisplay = contentDisplay;
        this.admin = Users.hasUserRole(userID, RoleID.RADA_KLANU);
        if (!this.admin) Users.isUserDev(userID);
        this.clanMember = Users.hasUserRole(userID, RoleID.CLAN_MEMBER_ID);
        this.userID = userID;
        this.isPrivate = event.isFromType(ChannelType.PRIVATE);
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

    public String getUserID() {
        return userID;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}

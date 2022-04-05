package server.service;

import embed.EmbedInfo;
import event.ButtonClickType;
import model.MemberWithPrivateChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.ArrayList;
import java.util.List;

public class ServerService {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private List<MemberWithPrivateChannel> reports = new ArrayList<>();
    private List<MemberWithPrivateChannel> unbans = new ArrayList<>();
    private List<MemberWithPrivateChannel> contacts = new ArrayList<>();

    public void buttonClick(ButtonClickEvent event, ButtonClickType buttonType) {
        String userID = event.getUser().getId();
        switch (buttonType) {
            case REPORT:
                if (!isUserOnList(userID, reports)) {

                } else {
                    EmbedInfo.cantCreateServerServiceChannel(userID);
                }
                break;
            case UNBAN:
                if (!isUserOnList(userID, unbans)) {

                } else {
                    EmbedInfo.cantCreateServerServiceChannel(userID);
                }
                break;
            case CONTACT:
                if (!isUserOnList(userID, contacts)) {

                } else {
                    EmbedInfo.cantCreateServerServiceChannel(userID);
                }
                break;
        }

    }

    private boolean isUserOnLists(String userID) {
        if (isUserOnList(userID, reports)) {
            return true;
        } else if (isUserOnList(userID, unbans)) {
            return true;
        } else {
            return isUserOnList(userID, contacts);
        }
    }

    private void addUserToList(String userID, List<MemberWithPrivateChannel> list) {

    }

    private boolean isUserOnList(String userID, List<MemberWithPrivateChannel> member) {
        for (MemberWithPrivateChannel m : member) {
            if (m.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

}

package model;

import event.ButtonClickType;

public class MemberServerService extends MemberWithPrivateChannel {

    private ButtonClickType buttonClickType;

    public MemberServerService(String userID, String userName, String channelID, ButtonClickType buttonClickType) {
        super(userID, userName, channelID);
        this.buttonClickType = buttonClickType;
    }

    public ButtonClickType getButtonClickType() {
        return buttonClickType;
    }
}

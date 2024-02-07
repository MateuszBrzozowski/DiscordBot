package pl.mbrzozowski.ranger.server.seed.call;

import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

public class LiveMessage extends MessageCall {

    protected LiveMessage(SettingsService settingsService) {
        super(1, settingsService, SettingsKey.SEED_CALL_LIVE, Type.LIVE);
        setMessages();
    }

    @Override
    void setMessages() {
        messages.add("Mamy LIVE na serwerze.");
    }

/*    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
//        Set<Choice> choiceList = getChoices();
//        commandData.add(getCommand(SlashCommands.SEED_CALL_LIVE_AMOUNT, choiceList)
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }*/
}

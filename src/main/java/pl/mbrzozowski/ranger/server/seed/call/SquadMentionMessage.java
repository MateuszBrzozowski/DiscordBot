package pl.mbrzozowski.ranger.server.seed.call;

import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

public class SquadMentionMessage extends MessageCall {

    protected SquadMentionMessage(SettingsService settingsService) {
        super(5, settingsService, SettingsKey.SEED_CALL_SQUAD, Type.SQUAD_MENTION);
        setMessages();
    }

    @Override
    void setMessages() {
        messages.add("@ Squad Zapraszmy na seed");
    }

/*    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
//        Set<Choice> choiceList = getChoices();
//        commandData.add(getCommand(SlashCommands.SEED_CALL_SQUAD_AMOUNT, choiceList)
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }*/
}

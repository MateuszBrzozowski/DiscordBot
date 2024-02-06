package pl.mbrzozowski.ranger.server.seed.call;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.ArrayList;
import java.util.Set;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class SquadMentionMessage extends MessageCall {

    protected SquadMentionMessage(SettingsService settingsService) {
        super(5, settingsService, SettingsKey.SEED_CALL_SQUAD, Type.SQUAD_MENTION);
        setMessages();
    }

    @Override
    void setMessages() {
        messages.add("@ Squad Zapraszmy na seed");
    }

    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
        Set<Choice> choiceList = getChoices();
        commandData.add(getCommand(SlashCommands.SEED_CALL_SQUAD_AMOUNT, choiceList)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }
}

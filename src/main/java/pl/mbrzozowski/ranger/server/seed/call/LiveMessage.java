package pl.mbrzozowski.ranger.server.seed.call;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.ArrayList;
import java.util.Set;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class LiveMessage extends MessageCall {

    protected LiveMessage(SettingsService settingsService) {
        super(1, settingsService, SettingsKey.SEED_CALL_LIVE_AMOUNT);
        setMessages();
    }

    @Override
    void setMessages() {
        messages.add("Mamy LIVE na serwerze.");
    }

    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
        Set<Choice> choiceList = getChoices();
        commandData.add(getCommand(SlashCommands.SEED_CALL_LIVE_AMOUNT, choiceList));
    }
}

package pl.mbrzozowski.ranger.server.seed.call;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.ArrayList;
import java.util.Set;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Component
public class LiveMessage extends MessageCall {

    private final SettingsKey settingsKeyPerDay = SettingsKey.SEED_CALL_LIVE_AMOUNT;


    protected LiveMessage(SettingsService settingsService) {
        super(1, settingsService);
        setMessages();
        setMessagePerDay();
    }

    @Override
    void setMessages() {
        messages.add("Mamy LIVE na serwerze.");
    }

    @Override
    void setMessagePerDay() {
        setMessagePerDay(settingsKeyPerDay);
    }

    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
        Set<Choice> choiceList = getChoices();
        commandData.add(getCommand(SlashCommands.SEED_CALL_LIVE_AMOUNT, choiceList));
    }

    public void setMaxAmount(SlashCommandInteractionEvent event) {
        setMaxAmount(event, settingsKeyPerDay);
    }
}

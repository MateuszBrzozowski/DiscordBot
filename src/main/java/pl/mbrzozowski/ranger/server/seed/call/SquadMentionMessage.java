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
public class SquadMentionMessage extends MessageCall {

    private final SettingsKey settingsKeyPerDay = SettingsKey.SEED_CALL_SQUAD_AMOUNT;

    protected SquadMentionMessage(SettingsService settingsService) {
        super(5, settingsService);
        setMessages();
        setMessagePerDay();
    }

    @Override
    void setMessages() {
        messages.add("@ Squad Zapraszmy na seed");
    }

    @Override
    void setMessagePerDay() {
        setMessagePerDay(settingsKeyPerDay);
    }

    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
        Set<Choice> choiceList = getChoices();
        commandData.add(getCommand(SlashCommands.SEED_CALL_SQUAD_AMOUNT, choiceList));
    }

    public void setMaxAmount(SlashCommandInteractionEvent event) {
        setMaxAmount(event, settingsKeyPerDay);
    }
}

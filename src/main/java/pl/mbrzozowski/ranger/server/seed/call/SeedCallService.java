package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.ArrayList;

@Slf4j
@Service
public class SeedCallService implements SlashCommand {

    private final SettingsService settingsService;
    private final MessageCall liveMessage;
    private final MessageCall squadMentionMessage;

    public SeedCallService(SettingsService settingsService) {
        this.settingsService = settingsService;
        this.liveMessage = new LiveMessage(settingsService);
        this.squadMentionMessage = new SquadMentionMessage(settingsService);
    }

    @Override
    public void getCommandsList(ArrayList<CommandData> commandData) {
        liveMessage.getCommandsList(commandData);
        squadMentionMessage.getCommandsList(commandData);
    }

    public void setMaxAmount(SlashCommandInteractionEvent event, @NotNull SettingsKey settingsKey) {
        if (settingsKey.equals(liveMessage.getSettingsKeyPerDay())) {
            liveMessage.setMaxAmount(event);
        } else if (settingsKey.equals(squadMentionMessage.getSettingsKeyPerDay())) {
            squadMentionMessage.setMaxAmount(event);
        }
    }
}


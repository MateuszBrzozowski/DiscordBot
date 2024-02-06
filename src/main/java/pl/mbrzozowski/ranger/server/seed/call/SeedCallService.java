package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.SEED_CALL_ENABLE;
import static pl.mbrzozowski.ranger.settings.SettingsKey.SEED_CALL;

@Slf4j
@Service
public class SeedCallService implements SlashCommand {

    private final SettingsService settingsService;
    private final MessageCall liveMessage;
    private final MessageCall squadMentionMessage;
    private boolean isEnable = false;

    public SeedCallService(SettingsService settingsService) {
        this.settingsService = settingsService;
        this.liveMessage = new LiveMessage(settingsService);
        this.squadMentionMessage = new SquadMentionMessage(settingsService);
        setOnOff();
    }

    @Override
    public void getCommandsList(ArrayList<CommandData> commandData) {
        liveMessage.getCommandsList(commandData);
        squadMentionMessage.getCommandsList(commandData);
        commandData.add(Commands.slash(SEED_CALL_ENABLE.getName(), SEED_CALL_ENABLE.getDescription())
                .addOptions(new OptionData(OptionType.BOOLEAN, "enable", SEED_CALL_ENABLE.getDescription())
                        .setRequired(true)));
    }

    public void setMaxAmount(SlashCommandInteractionEvent event, @NotNull SettingsKey settingsKey) {
        if (settingsKey.equals(liveMessage.getSettingsKeyPerDay())) {
            liveMessage.setMaxAmount(event);
        } else if (settingsKey.equals(squadMentionMessage.getSettingsKeyPerDay())) {
            squadMentionMessage.setMaxAmount(event);
        }
    }

    public void switchOnOff(@NotNull SlashCommandInteractionEvent event) {
        this.isEnable = Objects.requireNonNull(event.getOption("enable")).getAsBoolean();
        settingsService.save(SEED_CALL, String.valueOf(this.isEnable));
        event.reply("Seed call service enable: " + this.isEnable).setEphemeral(true).queue();
    }

    private void setOnOff() {
        Optional<String> optional = settingsService.find(SEED_CALL);
        if (optional.isEmpty()) {
            log.info("New settings property set {}={}", SEED_CALL, this.isEnable);
            settingsService.save(SEED_CALL, "false");
            return;
        }
        this.isEnable = Boolean.parseBoolean(optional.get());
        log.info("Seed call service: {}", this.isEnable);
    }
}


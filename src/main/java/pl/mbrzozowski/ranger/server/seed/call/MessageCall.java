package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Slf4j
public abstract class MessageCall implements SlashCommand {

    private final static String CHANNEL_ID = "123";
    protected final Set<String> messages = new HashSet<>();
    protected final int MAX_PER_DAY;
    protected int messagePerDayCount = 0;
    protected int messagePerDay = 0;
    private final SettingsService settingsService;

    protected MessageCall(int maxPerDay, SettingsService settingsService) {
        this.settingsService = settingsService;
        MAX_PER_DAY = maxPerDay;
    }

    abstract void setMessages();

    abstract void setMessagePerDay();

    protected void setMessagePerDay(SettingsKey settingsKey) {
        Optional<String> optional = settingsService.find(settingsKey);
        if (optional.isEmpty()) {
            log.info("New settings property set {}={}", settingsKey, 0);
            settingsService.save(settingsKey, 0);
            return;
        }
        try {
            this.messagePerDay = Integer.parseInt(optional.get());
        } catch (NumberFormatException e) {
            log.warn("Settings property \"{}\" incorrect. Set default value={}", settingsKey, 0);
            settingsService.save(settingsKey, 0);
            throw new RuntimeException(e);
        }
    }

    protected Set<Choice> getChoices() {
        Set<Choice> choices = new HashSet<>();
        for (int i = 0; i <= MAX_PER_DAY; i++) {
            Choice choice = new Choice(String.valueOf(i), i);
            choices.add(choice);
        }
        return choices;
    }

    protected CommandData getCommand(@NotNull SlashCommands command, Set<Choice> choiceList) {
        return Commands.slash(command.getName(), command.getDescription())
                .addOptions(new OptionData(OptionType.INTEGER, "count", "Ile razy na dzień. 0 - OFF")
                        .addChoices(choiceList)
                        .setRequired(true));
    }

    void setMaxAmount(@NotNull SlashCommandInteractionEvent event, SettingsKey settingsKey) {
        int count = Objects.requireNonNull(event.getOption("count")).getAsInt();
        if (count < 0 || count > MAX_PER_DAY) {
            event.reply("Niepoprawna wartość!").setEphemeral(true).queue();
            log.error("Option incorrect - {}", count);
            return;
        }
        settingsService.save(settingsKey, count);
        event.reply("Ustawiono maksymalną ilość wiadomości - " + count).setEphemeral(true).queue();
        log.info("Set setting property - {}={}", settingsKey, count);
    }
}

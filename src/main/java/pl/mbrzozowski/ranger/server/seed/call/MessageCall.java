package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Slf4j
public abstract class MessageCall implements SlashCommand {

    private final static String CHANNEL_ID = "123";
    private final static int MAX_CONDITIONS = 3;
    private final List<Conditions> conditions = new ArrayList<>();
    private final SettingsService settingsService;
    private final SettingsKey settingsKeyPerDay;
    protected final Set<String> messages = new HashSet<>();
    protected final int MAX_PER_DAY;
    protected int messagePerDayCount = 0;
    protected int messagePerDay = 0;
    private final Type type;

    protected MessageCall(int maxPerDay, SettingsService settingsService, SettingsKey settingsKeyPerDay, Type type) {
        this.settingsService = settingsService;
        MAX_PER_DAY = maxPerDay;
        this.settingsKeyPerDay = settingsKeyPerDay;
        this.type = type;
        setMessagePerDay();
        setConditions();
    }

    abstract void setMessages();

    public SettingsKey getSettingsKeyPerDay() {
        return settingsKeyPerDay;
    }

    private void setConditions() {
        if (type.equals(Type.LIVE)) {
            setConditions(SettingsKey.SEED_CALL_LIVE_CONDITIONS, "seed-call-live-");
        } else if (type.equals(Type.SQUAD_MENTION)) {
            setConditions(SettingsKey.SEED_CALL_SQUAD_CONDITIONS, "seed-call-squad-");
        }
    }

    private void setConditions(SettingsKey settingsKey, String keyPrefix) {
        Optional<String> optional = settingsService.find(settingsKey);
        if (optional.isPresent()) {
            String conditionsCount = optional.get();
            if (!conditionsCount.chars().allMatch(Character::isDigit)) {
                log.warn("{} incorrect. Cancel load conditions.", settingsKey);
                return;
            }
            conditionsCount.chars().allMatch(Character::isDigit);
            int size = Integer.parseInt(optional.get());
            for (int i = 0; i < size; i++) {
                String keyPlayers = keyPrefix + i + "-players";
                String keyTime = keyPrefix + i + "-time";
                Optional<String> players = settingsService.find(keyPlayers);
                Optional<String> time = settingsService.find(keyTime);
                if (players.isEmpty() || time.isEmpty()) {
                    log.warn("Setting properties for key empty: {} or {}", keyPlayers, keyTime);
                    continue;
                }
                if (players.get().chars().allMatch(Character::isDigit) && time.get().chars().allMatch(Character::isDigit)) {
                    Conditions condition = new Conditions(Integer.parseInt(players.get()), Integer.parseInt(time.get()));
                    conditions.add(condition);
                    log.info("Settings properties loaded {}, {}", keyPlayers, keyTime);
                }
            }
        }
    }

    protected void addOption(@NotNull SlashCommandInteractionEvent event) {
        if (conditions.size() >= MAX_CONDITIONS) {
            event.reply("Możesz ustawić maksymalnie 3 warunki.").setEphemeral(true).queue();
            return;
        }
        int players = Objects.requireNonNull(event.getOption("players")).getAsInt();
        int minutes = Objects.requireNonNull(event.getOption("minutes")).getAsInt();
        if (players < 0 || players > 100) {
            event.reply("Ilość graczy musi być z przedziału od 0 do 100").setEphemeral(true).queue();
            return;
        }
        if (minutes < 1 || minutes > 120) {
            event.reply("Minuty muszą być z przedziału od 1 do 120.").setEphemeral(true).queue();
            return;
        }
        Conditions condition = new Conditions(players, minutes);
        this.conditions.add(condition);
        event.reply("Warunek dodany. Jeżeli " + condition.getPlayersCount() + " graczy przez " +
                condition.getWithinMinutes() + " minut").setEphemeral(true).queue();
        saveSettings();
    }

    private void saveSettings() {
        if (type.equals(Type.LIVE)) {
            saveSettings(SettingsKey.SEED_CALL_LIVE_CONDITIONS, "seed-call-live-");
        } else if (type.equals(Type.SQUAD_MENTION)) {
            saveSettings(SettingsKey.SEED_CALL_SQUAD_CONDITIONS, "seed-call-squad-");
        }
    }

    private void saveSettings(SettingsKey settingsKey, String keyPrefix) {
        settingsService.save(settingsKey, conditions.size());
        for (int i = 0; i < conditions.size(); i++) {
            String keyPlayers = keyPrefix + i + "-players";
            String keyTime = keyPrefix + i + "-time";
            settingsService.save(keyPlayers, conditions.get(i).getPlayersCount());
            settingsService.save(keyTime, conditions.get(i).getWithinMinutes());
        }
    }

    public void removeOption(SlashCommandInteractionEvent event) {
        if (conditions.size() == 0) {
            event.reply("Brak warunków").setEphemeral(true).queue();
            return;
        }
        OptionMapping idOption = event.getOption("id");
        if (idOption == null) {
            if (conditions.size() == 1) {
                replySuccessfully(event, conditions.get(0));
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("**Więcej niż jeden warunek!** Wybierz ID. Wywołaj ponownie komendę z wybranym ID\n");
                for (int i = 1; i <= conditions.size(); i++) {
                    builder.append("ID: ").append(i).append(", Jeżeli ").append(conditions.get(i - 1).getPlayersCount())
                            .append(" graczy przez ").append(conditions.get(i - 1).getWithinMinutes()).append(" minut\n");
                }
                event.reply(builder.toString()).setEphemeral(true).queue();
            }
        } else {
            int id = idOption.getAsInt() - 1;
            if (id < 0 || id >= conditions.size()) {
                event.reply("Niepoprawne ID").setEphemeral(true).queue();
                return;
            }
            Conditions remove = conditions.remove(id);
            replySuccessfully(event, remove);
        }
    }

    private void replySuccessfully(@NotNull SlashCommandInteractionEvent event, @NotNull Conditions settings) {
        event.reply("Usunięto warunek: Jeżeli " + settings.getPlayersCount() +
                        " graczy przez " + settings.getWithinMinutes() + " minut")
                .setEphemeral(true)
                .queue();
        saveSettings();
        log.info("Condition {} removed", settings);
    }

    protected void setMessagePerDay() {
        Optional<String> optional = settingsService.find(settingsKeyPerDay);
        if (optional.isEmpty()) {
            log.info("New settings property set {}={}", settingsKeyPerDay, 0);
            settingsService.save(settingsKeyPerDay, 0);
            return;
        }
        try {
            this.messagePerDay = Integer.parseInt(optional.get());
            if (messagePerDay < 0 || messagePerDay > MAX_PER_DAY) {
                throw new IllegalArgumentException("Message per day " + messagePerDay);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Settings property \"{}\" incorrect. Set default value={}", settingsKeyPerDay, 0);
            settingsService.save(settingsKeyPerDay, 0);
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
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }

    void setMaxAmount(@NotNull SlashCommandInteractionEvent event) {
        int count = Objects.requireNonNull(event.getOption("count")).getAsInt();
        if (count < 0 || count > MAX_PER_DAY) {
            event.reply("Niepoprawna wartość!").setEphemeral(true).queue();
            log.error("Option incorrect - {}", count);
            return;
        }
        settingsService.save(settingsKeyPerDay, count);
        event.reply("Ustawiono maksymalną ilość wiadomości - " + count).setEphemeral(true).queue();
        log.info("Set setting property - {}={}", settingsKeyPerDay, count);
    }

    enum Type {
        SQUAD_MENTION,
        LIVE
    }
}

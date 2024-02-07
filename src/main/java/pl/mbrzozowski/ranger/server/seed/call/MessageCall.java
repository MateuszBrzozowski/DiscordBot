package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;
import static pl.mbrzozowski.ranger.helpers.SlashCommands.SEED_CALL_AMOUNT;

@Slf4j
public abstract class MessageCall implements SlashCommand {

    private final static String CHANNEL_ID = "1204551588925018112";
    private final static int MAX_CONDITIONS = 3;
    private static final int OFFSET = 3;
    private final List<Conditions> conditions = new ArrayList<>();
    private final SettingsService settingsService;
    private final SettingsKey settingsKeyPerDay;
    private final Type type;
    protected final int MAX_PER_DAY;
    protected final List<String> messages = new ArrayList<>();
    protected int messagePerDayCount = 0;
    protected int messagePerDay = 0;

    protected MessageCall(int maxPerDay, SettingsService settingsService, SettingsKey settingsKeyPerDay, Type type) {
        MAX_PER_DAY = maxPerDay;
        this.settingsService = settingsService;
        this.settingsKeyPerDay = settingsKeyPerDay;
        this.type = type;
        setMessagePerDayCount();
        setMessagePerDay();
        setConditions();
    }

    abstract void setMessages();

    public SettingsKey getSettingsKeyPerDay() {
        return settingsKeyPerDay;
    }

    public int getMessagePerDay() {
        return messagePerDay;
    }

    public int getMessagePerDayCount() {
        return messagePerDayCount;
    }

    private void setConditions() {
        if (type.equals(Type.LIVE)) {
            setConditions(SettingsKey.SEED_CALL_LIVE_CONDITIONS, "seed.call.live.");
        } else if (type.equals(Type.SQUAD_MENTION)) {
            setConditions(SettingsKey.SEED_CALL_SQUAD_CONDITIONS, "seed.call.squad.");
        }
    }

    @Override
    public void getCommandsList(ArrayList<CommandData> commandData) {
        Set<Choice> choiceList = getChoices();
        commandData.add(getCommand(choiceList)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
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
                String keyPlayers = keyPrefix + i + ".players";
                String keyTime = keyPrefix + i + ".time";
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
            saveSettings(SettingsKey.SEED_CALL_LIVE_CONDITIONS, "seed.call.live.");
        } else if (type.equals(Type.SQUAD_MENTION)) {
            saveSettings(SettingsKey.SEED_CALL_SQUAD_CONDITIONS, "seed.call.squad.");
        }
    }

    private void saveSettings(SettingsKey settingsKey, String keyPrefix) {
        settingsService.save(settingsKey, conditions.size());
        for (int i = 0; i < conditions.size(); i++) {
            String keyPlayers = keyPrefix + i + ".players";
            String keyTime = keyPrefix + i + ".time";
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
                Conditions remove = conditions.remove(0);
                replySuccessfully(event, remove);
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

    private void setMessagePerDayCount() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LIVE_COUNT);
        if (optional.isEmpty()) {
            settingsService.save(SettingsKey.SEED_CALL_LIVE_COUNT, 0);
            this.messagePerDayCount = 0;
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            settingsService.save(SettingsKey.SEED_CALL_LIVE_COUNT, 0);
            this.messagePerDayCount = 0;
            return;
        }
        this.messagePerDayCount = Integer.parseInt(optional.get());
    }

    @NotNull
    private Set<Choice> getChoices() {
        Set<Choice> choices = new HashSet<>();
        for (int i = 0; i <= 5; i++) {
            Choice choice = new Choice(String.valueOf(i), i);
            choices.add(choice);
        }
        return choices;
    }

    protected CommandData getCommand(Set<Choice> choiceList) {
        return Commands.slash(SEED_CALL_AMOUNT.getName(), SEED_CALL_AMOUNT.getDescription())
                .addOptions(new OptionData(OptionType.STRING, SlashCommands.TYPE.getName(), "Do którego typu chcesz zmienić liczbę wiadomości")
                        .addChoice("Live", SlashCommands.LIVE.getName())
                        .addChoice("Ping Squad", SlashCommands.PING_SQUAD.getName())
                        .setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, SlashCommands.COUNT.getName(), "Ile razy na dzień. 0 - OFF")
                        .addChoices(choiceList)
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }

    void setMaxAmount(@NotNull SlashCommandInteractionEvent event) {
        int count = Objects.requireNonNull(event.getOption("count")).getAsInt();
        if (count < 0 || count > MAX_PER_DAY) {
            event.reply("Niepoprawna wartość! Maksymalna - " + MAX_PER_DAY).setEphemeral(true).queue();
            log.error("Option incorrect - {}", count);
            return;
        }
        settingsService.save(settingsKeyPerDay, count);
        event.reply("Ustawiono maksymalną ilość wiadomości - " + count).setEphemeral(true).queue();
        log.info("Set setting property - {}={}", settingsKeyPerDay, count);
    }

    public String getConditions() {
        StringBuilder builder = new StringBuilder();
        if (type.equals(Type.LIVE)) {
            builder.append("Warunki dla wiadomości LIVE\n");
        } else if (type.equals(Type.SQUAD_MENTION)) {
            builder.append("Warunki dla wiadomości zo oznaczeniem roli Squad\n");
        }
        if (conditions.size() == 0) {
            builder.append("Brak warunków\n");
            return builder.toString();
        }
        for (Conditions condition : conditions) {
            builder.append("Jeżeli ").append(condition.getPlayersCount()).append(" graczy przez ").append(condition.getWithinMinutes())
                    .append(" minut\n");
        }
        return builder.toString();
    }

    public int getConditionsSize() {
        return conditions.size();
    }

    public boolean analyzeConditions(List<PlayerCounts> players) {
        for (Conditions condition : conditions) {
            LocalDateTime dateTime = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(condition.getWithinMinutes());
            players.removeIf(playerCounts -> playerCounts.getTime().isBefore(dateTime));
            if (players.size() == 0) {
                continue;
            }
            players.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
            List<PlayerCounts> playersAfterFilter = players.stream()
                    .filter(playerCounts -> playerCounts.getPlayers() < condition.getPlayersCount() - OFFSET).toList();
            if (playersAfterFilter.size() <= 0 || playersAfterFilter.size() <= players.size() / 10) {
                return true;
            }
        }
        return false;
    }

    public void addMessagePerDayCount() {
        this.messagePerDayCount++;
        if (type.equals(Type.LIVE)) {
            settingsService.save(SettingsKey.SEED_CALL_LIVE_COUNT, this.messagePerDayCount);
        } else if (type.equals(Type.SQUAD_MENTION)) {
            settingsService.save(SettingsKey.SEED_CALL_SQUAD_COUNT, this.messagePerDayCount);
        }
    }

    public void sendMessage() {
        Random random = new Random();
        int nextInt = random.nextInt(messages.size());
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            log.warn("Null guild");
            return;
        }
        TextChannel textChannel = guild.getTextChannelById(CHANNEL_ID);
        if (textChannel == null) {
            log.warn("Null channel");
            return;
        }
        textChannel.sendMessage(messages.get(nextInt)).queue();
        log.info("Sent seed call message");
    }

    public void resetMessageCount() {
        messagePerDayCount = 0;
        if (type.equals(Type.LIVE)) {
            settingsService.save(SettingsKey.SEED_CALL_LIVE_COUNT, 0);
        } else if (type.equals(Type.SQUAD_MENTION)) {
            settingsService.save(SettingsKey.SEED_CALL_SQUAD_COUNT, 0);
        }
    }

    enum Type {
        SQUAD_MENTION,
        LIVE
    }
}

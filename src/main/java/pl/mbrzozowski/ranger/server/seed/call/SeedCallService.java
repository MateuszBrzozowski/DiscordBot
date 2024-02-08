package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.SlashCommands;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;
import pl.mbrzozowski.ranger.stats.service.PlayerCountsService;

import java.time.LocalDateTime;
import java.util.*;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.*;
import static pl.mbrzozowski.ranger.settings.SettingsKey.SEED_CALL;
import static pl.mbrzozowski.ranger.settings.SettingsKey.SEED_CALL_LEVEL;

@Slf4j
@Service
public class SeedCallService implements SlashCommand {

    private final PlayerCountsService playerCountsService;
    private final SettingsService settingsService;
    private final MessageCall[] messageCalls = new MessageCall[4];
    private boolean isEnable = false;
    private Levels level;
    private Timer timer;

    public SeedCallService(SettingsService settingsService, PlayerCountsService playerCountsService) {
        this.playerCountsService = playerCountsService;
        this.settingsService = settingsService;
        createLevels();
        pullOnOff();
        pullLevels();
        pullLast();
    }

    public void run() {
        if (!isEnable) {
            log.info("Seed call service disable");
            return;
        }
        List<PlayerCounts> players = playerCountsService.findLastDay();
        checkLevel(Levels.ONE, players);
    }

    private void checkLevel(@NotNull Levels level, List<PlayerCounts> players) {
        switch (level) {
            case ONE -> {
                if (messageCalls[0].analyzeConditionsWhileStart(players)) {
                    setLevel(Levels.TWO);
                    checkLevel(Levels.TWO, players);
                }
            }
            case TWO -> {
                if (messageCalls[1].analyzeConditionsWhileStart(players)) {
                    setLevel(Levels.THREE);
                    checkLevel(Levels.THREE, players);
                }
            }
            case THREE -> {
                if (messageCalls[2].analyzeConditionsWhileStart(players)) {
                    setLevel(Levels.FOUR);
                    checkLevel(Levels.FOUR, players);
                }
            }
            case FOUR -> {
                if (messageCalls[3].analyzeConditionsWhileStart(players)) {
                    setLevel(Levels.END);
                    start();
                }
            }
            case END -> start();
            default -> throw new UnsupportedOperationException(String.valueOf(level));
        }
    }

    private void start() {
        log.info("Seed call service enable");
        timer = new Timer();
        SeedCallExecute seedCallExecute = new SeedCallExecute(this);
        Calendar calendar = Calendar.getInstance();
        timer.scheduleAtFixedRate(seedCallExecute, calendar.getTime(), 60 * 1000);
    }

    private void createLevels() {
        LevelFactory levelFactory = new LevelFactory();
        Levels[] levels = Levels.values();
        for (int i = 0; i < messageCalls.length; i++) {
            messageCalls[i] = levelFactory.getLevelOfMessageCall(levels[i], settingsService);
        }
    }

    private void pullLevels() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LEVEL);
        if (optional.isEmpty()) {
            log.info("No settings property");
            setLevel(Levels.ONE);
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            log.info("Settings property incorrect");
            setLevel(Levels.ONE);
            return;
        }
        level = Levels.getLevel(Integer.parseInt(optional.get()));
        log.info("Levels set from settings: {}", level);
    }

    private void pullOnOff() {
        Optional<String> optional = settingsService.find(SEED_CALL);
        if (optional.isEmpty()) {
            log.info("No settings property. Seed call service disable");
            settingsService.save(SEED_CALL, "false");
            return;
        }
        this.isEnable = Boolean.parseBoolean(optional.get());
        log.info("Seed call service property from settings: {}", this.isEnable);
    }

    private void pullLast() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LAST);
        if (optional.isEmpty()) {
            setLevel(Levels.ONE);
            log.info("No settings property. {}", SettingsKey.SEED_CALL_LAST);
            settingsService.save(SettingsKey.SEED_CALL_LAST, LocalDateTime.now().getDayOfYear());
        }
    }

    private void setLevel(@NotNull Levels level) {
        this.level = level;
        log.info("Levels set: " + level);
        settingsService.save(SEED_CALL_LEVEL, level.getLevel());
    }

    @Override
    public void getCommandsList(@NotNull ArrayList<CommandData> commandData) {
        Set<Command.Choice> choiceList = getChoices();
        commandData.add(Commands.slash(SEED_CALL_AMOUNT.getName(), SEED_CALL_AMOUNT.getDescription())
                .addOptions(new OptionData(OptionType.STRING, SlashCommands.LEVEL.getName(), "Do którego typu chcesz zmienić liczbę wiadomości")
                        .addChoices(getChoicesForLevels())
                        .setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, SlashCommands.COUNT.getName(), "Ile razy na dzień. 0 - OFF")
                        .addChoices(choiceList)
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(Commands.slash(SEED_CALL_ENABLE.getName(), SEED_CALL_ENABLE.getDescription())
                .addOptions(new OptionData(OptionType.BOOLEAN, "enable", SEED_CALL_ENABLE.getDescription())
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_CONDITIONS.getName(), SlashCommands.SEED_CALL_CONDITIONS.getDescription())
                        .addOptions(new OptionData(OptionType.STRING, SlashCommands.LEVEL.getName(), "Do którego levelu chcesz dodać warunek")
                                .addChoices(getChoicesForLevels())
                                .setRequired(true))
                        .addOption(OptionType.INTEGER, "players", "Ilość graczy", true)
                        .addOption(OptionType.INTEGER, "minutes", "Minuty przez jaki okres", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_CONDITIONS_REMOVE.getName(), SlashCommands.SEED_CALL_CONDITIONS_REMOVE.getDescription())
                        .addOptions(new OptionData(OptionType.STRING, SlashCommands.LEVEL.getName(), "Z którego typu chcesz usunąć warunek")
                                .addChoices(getChoicesForLevels())
                                .setRequired(true))
                        .addOption(OptionType.INTEGER, "id", "ID warunku", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(Commands.slash(SEED_CALL_CONDITIONS_INFO.getName(), SEED_CALL_CONDITIONS_INFO.getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }

    @NotNull
    private Set<Command.Choice> getChoices() {
        Set<Command.Choice> choices = new HashSet<>();
        for (int i = 0; i <= MessageCall.MAX_PER_DAY; i++) {
            Command.Choice choice = new Command.Choice(String.valueOf(i), i);
            choices.add(choice);
        }
        return choices;
    }

    @NotNull
    private Set<Command.Choice> getChoicesForLevels() {
        Set<Command.Choice> choices = new HashSet<>();
        for (int i = 1; i <= 4; i++) {
            Command.Choice choice = new Command.Choice("Level " + i, i);
            choices.add(choice);
        }
        return choices;
    }

    /**
     * @param event    of {@link SlashCommandInteractionEvent}
     * @param whatToDo 0 - Set max amount; 1 - Add Conditions; 2 - Remove Conditions
     * @param index    of {@link MessageCall} where MIN 0 - level 1 | MAX 3 - level 4
     * @throws IndexOutOfBoundsException     when index is less than 0, or greater than 3
     * @throws UnsupportedOperationException when whatToDo is not 0,1 or 2
     */
    private void setAttributes(@NotNull SlashCommandInteractionEvent event, int whatToDo, int index) {
        switch (whatToDo) {
            case 0 -> messageCalls[index].setMaxAmount(event);
            case 1 -> messageCalls[index].addConditions(event);
            case 2 -> messageCalls[index].removeOption(event);
            default -> throw new UnsupportedOperationException(String.valueOf(whatToDo));
        }
    }

    /**
     * @param event    of {@link SlashCommandInteractionEvent}
     * @param whatToDo 0 - Set max amount; 1 - Add Conditions; 2 - Remove Conditions
     * @throws UnsupportedOperationException when selected level by event is less than 0, or greater than 3
     */
    private void setAttributes(@NotNull SlashCommandInteractionEvent event, int whatToDo) {
        int level = getLevelAsNumber(event).orElse(-1);
        switch (level) {
            case 1 -> setAttributes(event, whatToDo, 0);
            case 2 -> setAttributes(event, whatToDo, 1);
            case 3 -> setAttributes(event, whatToDo, 2);
            case 4 -> setAttributes(event, whatToDo, 3);
            default -> throw new UnsupportedOperationException(String.valueOf(level));
        }
    }

    public void setMaxAmount(@NotNull SlashCommandInteractionEvent event) {
        setAttributes(event, 0);
    }

    public void addConditions(@NotNull SlashCommandInteractionEvent event) {
        setAttributes(event, 1);
    }

    public void removeConditions(@NotNull SlashCommandInteractionEvent event) {
        setAttributes(event, 2);
    }

    public void switchOnOff(@NotNull SlashCommandInteractionEvent event) {
        if (timer != null) {
            timer.cancel();
            log.info("Timer cancel");
            timer = null;
        }
        this.isEnable = Objects.requireNonNull(event.getOption("enable")).getAsBoolean();
        settingsService.save(SEED_CALL, String.valueOf(this.isEnable));
        event.reply("Seed call service enable: " + this.isEnable).setEphemeral(true).queue();
        if (isEnable) {
            start();
        }
    }

    private Optional<Integer> getLevelAsNumber(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption(LEVEL.getName());
        if (option == null) {
            event.reply("Nieprawidłowy typ").setEphemeral(true).queue();
            log.error("Null option");
            return Optional.empty();
        }
        int level = option.getAsInt();
        return Optional.of(level);
    }

    public void conditionsInfo(@NotNull SlashCommandInteractionEvent event) {
        StringBuilder builder = new StringBuilder();
        for (MessageCall messageCall : messageCalls) {
            builder.append(messageCall.getConditions());
        }
        event.reply(builder.toString()).setEphemeral(true).queue();
        log.info("Conditions info sent");
    }

    protected PlayerCountsService getPlayerCountsService() {
        return playerCountsService;
    }

    protected void analyze(List<PlayerCounts> players) {
        switch (level) {
            case ONE -> {
                if (checkConditionsAndMsgPerDay(messageCalls[0], Levels.TWO)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(messageCalls[0], players);
                verifyMessageCount(messageCalls[0], Levels.TWO);
            }
            case TWO -> {
                if (checkConditionsAndMsgPerDay(messageCalls[1], Levels.THREE)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(messageCalls[1], players);
                verifyMessageCount(messageCalls[1], Levels.THREE);
            }
            case THREE -> {
                if (checkConditionsAndMsgPerDay(messageCalls[2], Levels.FOUR)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(messageCalls[2], players);
                verifyMessageCount(messageCalls[2], Levels.FOUR);
            }
            case FOUR -> {
                if (checkConditionsAndMsgPerDay(messageCalls[3], Levels.END)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(messageCalls[3], players);
                verifyMessageCount(messageCalls[3], Levels.END);
            }
            case END -> {
                if (!checkConditionsAndMsgPerDay()) {
                    return;
                }
            }
            default -> throw new UnsupportedOperationException(level + " Not supported");
        }
        settingsService.save(SettingsKey.SEED_CALL_LAST, LocalDateTime.now().getDayOfYear());
    }

    private void analyzeConditions(@NotNull MessageCall messageCall, List<PlayerCounts> players) {
        if (messageCall.analyzeConditions(players)) {
            messageCall.sendMessage();
            messageCall.addMessagePerDayCount();
            log.info("Seed call message sent");
        } else {
            log.info("Any conditions not fulfilled");
        }
    }

    private void verifyMessageCount(@NotNull MessageCall messageCall, Levels level) {
        if (messageCall.getMessagePerDay() == messageCall.getMessagePerDayCount()) {
            setLevel(level);
        }
    }

    private boolean checkConditionsAndMsgPerDay() {
        if (checkAllConditions()) {
            isEnable = false;
            timer.cancel();
            setLevel(Levels.ONE);
            settingsService.save(SEED_CALL, "false");
            return false;
        }
        return true;
    }

    private boolean checkAllConditions() {
        boolean level1 = checkAllConditions(0);
        boolean level2 = checkAllConditions(1);
        boolean level3 = checkAllConditions(2);
        boolean level4 = checkAllConditions(3);
        return level1 && level2 && level3 && level4;
    }

    private boolean checkAllConditions(int index) {
        return messageCalls[index].getConditionsSize() == 0 || messageCalls[index].getMessagePerDay() == 0;
    }

    private boolean checkConditionsAndMsgPerDay(@NotNull MessageCall messageCall, Levels level) {
        if (messageCall.getConditionsSize() == 0 || messageCall.getMessagePerDay() == 0 ||
                messageCall.getMessagePerDayCount() >= MessageCall.MAX_PER_DAY) {
            setLevel(level);
            return true;
        }
        return false;
    }

    public void resetLevels() {
        for (MessageCall messageCall : messageCalls) {
            messageCall.resetMessageCount();
        }
        setLevel(Levels.ONE);
    }
}


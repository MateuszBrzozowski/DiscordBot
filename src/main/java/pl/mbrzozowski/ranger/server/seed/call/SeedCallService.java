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
import static pl.mbrzozowski.ranger.settings.SettingsKey.SEED_CALL_MODE;

@Slf4j
@Service
public class SeedCallService implements SlashCommand {

    private final PlayerCountsService playerCountsService;
    private final SettingsService settingsService;
    private final MessageCall squadMentionMessage;
    private final MessageCall liveMessage;
    private boolean isEnable = false;
    private Mode mode;
    private Timer timer;

    public SeedCallService(SettingsService settingsService, PlayerCountsService playerCountsService) {
        this.playerCountsService = playerCountsService;
        this.settingsService = settingsService;
        this.liveMessage = new LiveMessage(settingsService);
        this.squadMentionMessage = new SquadMentionMessage(settingsService);
        pullOnOff();
        pullMode();
        pullLast();
    }

    public void run() {
        if (!isEnable) {
            log.info("Seed call service disable");
            return;
        }
        log.info("Seed call service enable");
        timer = new Timer();
        SeedCallExecute seedCallExecute = new SeedCallExecute(this);
        Calendar calendar = Calendar.getInstance();
        timer.scheduleAtFixedRate(seedCallExecute, calendar.getTime(), 60 * 1000);
    }

    private void pullMode() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_MODE);
        if (optional.isEmpty()) {
            log.info("No settings property");
            setMode(Mode.SQUAD);
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            log.info("Settings property incorrect");
            setMode(Mode.SQUAD);
            return;
        }
        mode = Mode.getMode(Integer.parseInt(optional.get()));
        log.info("Mode set from settings: {}", mode);
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
            setMode(Mode.SQUAD);
            log.info("No settings property. {}", SettingsKey.SEED_CALL_LAST);
            settingsService.save(SettingsKey.SEED_CALL_LAST, LocalDateTime.now().getDayOfYear());
        }
    }

    private void setMode(@NotNull Mode mode) {
        this.mode = mode;
        log.info("Mode set: " + mode);
        settingsService.save(SEED_CALL_MODE, mode.getMode());
    }

    @Override
    public void getCommandsList(ArrayList<CommandData> commandData) {
        squadMentionMessage.getCommandsList(commandData);
        commandData.add(Commands.slash(SEED_CALL_ENABLE.getName(), SEED_CALL_ENABLE.getDescription())
                .addOptions(new OptionData(OptionType.BOOLEAN, "enable", SEED_CALL_ENABLE.getDescription())
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_CONDITIONS.getName(), SlashCommands.SEED_CALL_CONDITIONS.getDescription())
                        .addOptions(new OptionData(OptionType.STRING, SlashCommands.TYPE.getName(), "Do którego typu chcesz dodać warunek")
                                .addChoice("Live", SlashCommands.LIVE.getName())
                                .addChoice("Ping Squad", SlashCommands.PING_SQUAD.getName())
                                .setRequired(true))
                        .addOption(OptionType.INTEGER, "players", "Ilość graczy", true)
                        .addOption(OptionType.INTEGER, "minutes", "Minuty przez jaki okres", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_CONDITIONS_REMOVE.getName(), SlashCommands.SEED_CALL_CONDITIONS_REMOVE.getDescription())
                        .addOptions(new OptionData(OptionType.STRING, SlashCommands.TYPE.getName(), "Z którego typu chcesz usunąć warunek")
                                .addChoice("Live", SlashCommands.LIVE.getName())
                                .addChoice("Ping Squad", SlashCommands.PING_SQUAD.getName())
                                .setRequired(true))
                        .addOption(OptionType.INTEGER, "id", "ID warunku", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(Commands.slash(SEED_CALL_CONDITIONS_INFO.getName(), SEED_CALL_CONDITIONS_INFO.getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }

    /**
     * @param event    of {@link SlashCommandInteractionEvent}
     * @param whatToDo 0 - Set max amount; 1 - Add Conditions; 2 - Remove Conditions
     */
    private void setAttributes(@NotNull SlashCommandInteractionEvent event, int whatToDo) {
        String type = getTypeAsString(event).orElse("");
        if (type.equalsIgnoreCase(LIVE.getName())) {
            switch (whatToDo) {
                case 0 -> {
                    log.info("Set max amount for {}", type);
                    liveMessage.setMaxAmount(event);
                }
                case 1 -> {
                    log.info("Add conditions for {}", type);
                    liveMessage.addConditions(event);
                }
                case 2 -> {
                    log.info("Remove conditions for {}", type);
                    liveMessage.removeOption(event);
                }
                default -> throw new IllegalArgumentException("Operation not supported");
            }

        } else if (type.equalsIgnoreCase(PING_SQUAD.getName())) {
            switch (whatToDo) {
                case 0 -> {
                    log.info("Set max amount for {}", type);
                    squadMentionMessage.setMaxAmount(event);
                }
                case 1 -> {
                    log.info("Add conditions for {}", type);
                    squadMentionMessage.addConditions(event);
                }
                case 2 -> {
                    log.info("Remove conditions for {}", type);
                    squadMentionMessage.removeOption(event);
                }
                default -> throw new IllegalArgumentException("Operation not supported");
            }

        } else {
            throw new IllegalArgumentException("Type not supported: " + type);
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
        run();
    }

    private Optional<String> getTypeAsString(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption(TYPE.getName());
        if (option == null) {
            event.reply("Nieprawidłowy typ").setEphemeral(true).queue();
            log.error("Null option");
            return Optional.empty();
        }
        String type = option.getAsString();
        return Optional.of(type);
    }

    public void conditionsInfo(@NotNull SlashCommandInteractionEvent event) {
        String liveConditions = liveMessage.getConditions();
        String squadConditions = squadMentionMessage.getConditions();
        event.reply(liveConditions + squadConditions).setEphemeral(true).queue();
        log.info("Conditions info sent");
    }

    protected PlayerCountsService getPlayerCountsService() {
        return playerCountsService;
    }

    protected void analyze(List<PlayerCounts> players) {
        switch (mode) {
            case SQUAD -> {
                if (checkConditionsAndMsgPerDay(squadMentionMessage, Mode.LIVE)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(squadMentionMessage, players);
                verifyMessageCount(squadMentionMessage, Mode.LIVE);
            }
            case LIVE -> {
                if (checkConditionsAndMsgPerDay(liveMessage, Mode.END)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(liveMessage, players);
                verifyMessageCount(liveMessage, Mode.END);
            }
            case END -> {
                if (!checkConditionsAndMsgPerDay()) {
                    return;
                }
            }
            default -> throw new IllegalStateException(mode + " Not supported");
        }
        settingsService.save(SettingsKey.SEED_CALL_LAST, LocalDateTime.now().getDayOfYear());
    }

    protected void checkDay() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LAST);
        if (optional.isEmpty()) {
            setMode(Mode.SQUAD);
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            setMode(Mode.SQUAD);
            return;
        }
        if (Integer.parseInt(optional.get()) != LocalDateTime.now().getDayOfYear()) {
            setMode(Mode.SQUAD);
            resetMode();
        }
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

    private void verifyMessageCount(@NotNull MessageCall messageCall, Mode mode) {
        if (messageCall.getMessagePerDay() == messageCall.getMessagePerDayCount()) {
            setMode(mode);
        }
    }

    private boolean checkConditionsAndMsgPerDay() {
        if ((squadMentionMessage.getConditionsSize() == 0 || squadMentionMessage.getMessagePerDay() == 0) &&
                (liveMessage.getConditionsSize() == 0 || liveMessage.getMessagePerDay() == 0)) {
            isEnable = false;
            timer.cancel();
            setMode(Mode.SQUAD);
            settingsService.save(SEED_CALL, "false");
            return false;
        }
        return true;
    }

    private boolean checkConditionsAndMsgPerDay(@NotNull MessageCall messageCall, Mode mode) {
        if (messageCall.getConditionsSize() == 0 || messageCall.getMessagePerDay() == 0 ||
                messageCall.getMessagePerDayCount() >= messageCall.MAX_PER_DAY) {
            setMode(mode);
            return true;
        }
        return false;
    }

    public void resetMode() {
        liveMessage.resetMessageCount();
        squadMentionMessage.resetMessageCount();
        setMode(Mode.SQUAD);
    }

    enum Mode {
        SQUAD(1),
        LIVE(2),
        END(0);

        private final int mode;
        private static final Mode[] ENUMS = Mode.values();

        Mode(int mode) {
            this.mode = mode;
        }

        int getMode() {
            return mode;
        }

        @NotNull
        public static Mode getMode(int mode) {
            for (Mode anEnum : ENUMS) {
                if (anEnum.getMode() == mode) {
                    return anEnum;
                }
            }
            throw new IllegalArgumentException(mode + " - Not supported");
        }
    }
}


package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
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

import static pl.mbrzozowski.ranger.helpers.SlashCommands.SEED_CALL_CONDITIONS_INFO;
import static pl.mbrzozowski.ranger.helpers.SlashCommands.SEED_CALL_ENABLE;
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
        setOnOff();
        setMode();
        setLast();

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
        timer.scheduleAtFixedRate(seedCallExecute, calendar.getTime(), 5 * 60 * 1000);
    }

    private void setMode() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_MODE);
        if (optional.isEmpty()) {
            settingsService.save(SEED_CALL_MODE, Mode.SQUAD.getMode());
            mode = Mode.SQUAD;
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            settingsService.save(SEED_CALL_MODE, Mode.SQUAD.getMode());
            mode = Mode.SQUAD;
            return;
        }
        mode = Mode.getMode(Integer.parseInt(optional.get()));
    }

    @Override
    public void getCommandsList(ArrayList<CommandData> commandData) {
        liveMessage.getCommandsList(commandData);
        squadMentionMessage.getCommandsList(commandData);
        commandData.add(Commands.slash(SEED_CALL_ENABLE.getName(), SEED_CALL_ENABLE.getDescription())
                .addOptions(new OptionData(OptionType.BOOLEAN, "enable", SEED_CALL_ENABLE.getDescription())
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_SQUAD_OPTION.getName(), SlashCommands.SEED_CALL_SQUAD_OPTION.getDescription())
                        .addOption(OptionType.INTEGER, "players", "Ilość graczy", true)
                        .addOption(OptionType.INTEGER, "minutes", "Minuty przez jaki okres", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_LIVE_OPTION.getName(), SlashCommands.SEED_CALL_LIVE_OPTION.getDescription())
                        .addOption(OptionType.INTEGER, "players", "Ilość graczy", true)
                        .addOption(OptionType.INTEGER, "minutes", "Minuty przez jaki okres", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_LIVE_OPTION_REMOVE.getName(), SlashCommands.SEED_CALL_LIVE_OPTION_REMOVE.getDescription())
                        .addOption(OptionType.INTEGER, "id", "ID warunku", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(
                Commands.slash(SlashCommands.SEED_CALL_SQUAD_OPTION_REMOVE.getName(), SlashCommands.SEED_CALL_SQUAD_OPTION_REMOVE.getDescription())
                        .addOption(OptionType.INTEGER, "id", "ID warunku", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
        commandData.add(Commands.slash(SEED_CALL_CONDITIONS_INFO.getName(), SEED_CALL_CONDITIONS_INFO.getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }

    public void setMaxAmount(SlashCommandInteractionEvent event, @NotNull SettingsKey settingsKey) {
        if (settingsKey.equals(liveMessage.getSettingsKeyPerDay())) {
            liveMessage.setMaxAmount(event);
        } else if (settingsKey.equals(squadMentionMessage.getSettingsKeyPerDay())) {
            squadMentionMessage.setMaxAmount(event);
        }
    }

    public void switchOnOff(@NotNull SlashCommandInteractionEvent event) {
        if (timer != null) {
            timer.cancel();
        }
        this.isEnable = Objects.requireNonNull(event.getOption("enable")).getAsBoolean();
        settingsService.save(SEED_CALL, String.valueOf(this.isEnable));
        event.reply("Seed call service enable: " + this.isEnable).setEphemeral(true).queue();
        if (isEnable) {
            run();
        }
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

    private void setLast() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LAST);
        if (optional.isEmpty()) {
            mode = Mode.SQUAD;
            settingsService.save(SEED_CALL_MODE, mode.getMode());
            settingsService.save(SettingsKey.SEED_CALL_LAST, LocalDateTime.now().getDayOfYear());
        }
    }

    public void addOption(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(SlashCommands.SEED_CALL_LIVE_OPTION.getName())) {
            liveMessage.addOption(event);
        } else if (event.getName().equals(SlashCommands.SEED_CALL_SQUAD_OPTION.getName())) {
            squadMentionMessage.addOption(event);
        }
    }

    public void removeOption(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(SlashCommands.SEED_CALL_LIVE_OPTION_REMOVE.getName())) {
            liveMessage.removeOption(event);
        } else if (event.getName().equals(SlashCommands.SEED_CALL_SQUAD_OPTION_REMOVE.getName())) {
            squadMentionMessage.removeOption(event);
        }
    }

    public void conditionsInfo(@NotNull SlashCommandInteractionEvent event) {
        String liveConditions = liveMessage.getConditions();
        String squadConditions = squadMentionMessage.getConditions();
        event.reply(liveConditions + squadConditions).setEphemeral(true).queue();
    }

    PlayerCountsService getPlayerCountsService() {
        return playerCountsService;
    }

    void analyze(List<PlayerCounts> players) {
        switch (mode) {
            case SQUAD -> {
                if (!checkConditionsAndMsgPerDay(squadMentionMessage, Mode.LIVE)) {
                    analyze(players);
                    return;
                }
                analyzeConditions(squadMentionMessage, players);
                verifyMessageCount(squadMentionMessage, Mode.LIVE);
            }
            case LIVE -> {
                if (!checkConditionsAndMsgPerDay(liveMessage, Mode.END)) {
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
            mode = Mode.SQUAD;
            settingsService.save(SEED_CALL_MODE, mode.getMode());
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            mode = Mode.SQUAD;
            settingsService.save(SEED_CALL_MODE, mode.getMode());
            return;
        }
        if (Integer.parseInt(optional.get()) != LocalDateTime.now().getDayOfYear()) {
            mode = Mode.SQUAD;
            resetMode();
        }
    }

    private void analyzeConditions(@NotNull MessageCall messageCall, List<PlayerCounts> players) {
        if (messageCall.analyzeConditions(players)) {
            messageCall.sendMessage();
            messageCall.addMessagePerDayCount();
        }
    }

    private void verifyMessageCount(@NotNull MessageCall messageCall, Mode mode) {
        if (messageCall.getMessagePerDay() == messageCall.getMessagePerDayCount()) {
            this.mode = mode;
            settingsService.save(SEED_CALL_MODE, mode.getMode());
        }
    }

    private boolean checkConditionsAndMsgPerDay() {
        if ((squadMentionMessage.getConditionsSize() == 0 || squadMentionMessage.getMessagePerDay() == 0) &&
                (liveMessage.getConditionsSize() == 0 || liveMessage.getMessagePerDay() == 0)) {
            isEnable = false;
            timer.cancel();
            mode = Mode.SQUAD;
            settingsService.save(SEED_CALL_MODE, Mode.SQUAD.getMode());
            settingsService.save(SEED_CALL, "false");
            return false;
        }
        return true;
    }

    private boolean checkConditionsAndMsgPerDay(@NotNull MessageCall messageCall, Mode mode) {
        if (messageCall.getConditionsSize() == 0 || messageCall.getMessagePerDay() == 0 ||
                messageCall.getMessagePerDayCount() >= messageCall.MAX_PER_DAY) {
            settingsService.save(SEED_CALL_MODE, mode.getMode());
            this.mode = mode;
            return false;
        }
        return true;
    }

    public void resetMode() {
        liveMessage.resetMessageCount();
        squadMentionMessage.resetMessageCount();
        this.mode = Mode.SQUAD;
        settingsService.save(SEED_CALL_MODE, this.mode.getMode());
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


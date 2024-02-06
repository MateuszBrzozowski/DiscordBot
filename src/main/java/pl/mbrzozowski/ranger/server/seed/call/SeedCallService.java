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
import pl.mbrzozowski.ranger.stats.service.PlayerCountsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.SEED_CALL_CONDITIONS_INFO;
import static pl.mbrzozowski.ranger.helpers.SlashCommands.SEED_CALL_ENABLE;
import static pl.mbrzozowski.ranger.settings.SettingsKey.SEED_CALL;

@Slf4j
@Service
public class SeedCallService implements SlashCommand {

    private final PlayerCountsService playerCountsService;
    private final SettingsService settingsService;
    private final MessageCall squadMentionMessage;
    private final MessageCall liveMessage;
    private boolean isEnable = false;

    public SeedCallService(SettingsService settingsService, PlayerCountsService playerCountsService) {
        this.playerCountsService = playerCountsService;
        this.settingsService = settingsService;
        this.liveMessage = new LiveMessage(settingsService);
        this.squadMentionMessage = new SquadMentionMessage(settingsService);
        setOnOff();
    }

    public void run() {
        if (!isEnable) {
            log.info("Seed call service disable");
            return;
        }
        log.info("Seed call service enable");
        Timer timer = new Timer();
        SeedCallExecute seedCallExecute = new SeedCallExecute(playerCountsService);
        Calendar calendar = Calendar.getInstance();
        calendar.set(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, LocalDate.now().getDayOfMonth());
        LocalDateTime dateTime2200 = LocalDateTime.now().withHour(22).withMinute(0);
        if (LocalDateTime.now().isAfter(dateTime2200)) {
            calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        timer.scheduleAtFixedRate(seedCallExecute, calendar.getTime(), 5 * 60 * 1000);
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
}


package pl.mbrzozowski.ranger.games;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.exceptions.IllegalSettingsException;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.guild.SlashCommands;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class RangerOfTheDay implements SlashCommandGame {

    private final SettingsService settingsService;

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        Optional<String> optionalDay = settingsService.find(SettingsKey.RANGER_OF_THE_DAY);
        Optional<String> optionalChannelId = settingsService.find(SettingsKey.RANGER_OF_THE_DAY_CHANNEL);
        Optional<String> optionalMessageId = settingsService.find(SettingsKey.RANGER_OF_THE_DAY_MESSAGE);
        if (optionalDay.isPresent() && optionalChannelId.isPresent() && optionalMessageId.isPresent()) {
            checkSettingsValue(event, optionalDay.get(), optionalChannelId.get(), optionalMessageId.get());
            String[] date = optionalDay.get().split("\\.");
            if (LocalDateTime.now().getDayOfYear() == Integer.parseInt(date[0]) &&
                    LocalDateTime.now().getYear() == Integer.parseInt(date[1])) {
                event.reply("Dzisiejszy Ranger został już wylosowany.\n" + "Wiadomość znajdziesz tutaj: " +
                        RangersGuild.getLinkToMessage(optionalChannelId.get(), optionalMessageId.get())).setEphemeral(true).queue();
                log.info("Day from settings={}, Current day={}, Ranger of the day exists.",
                        optionalDay.get(), LocalDateTime.now());
            } else {
                drawAndShow(event);
            }
        } else {
            drawAndShow(event);
        }
    }

    private void checkSettingsValue(@NotNull SlashCommandInteractionEvent event, String dayOfYear, String channelId, String messageId) {
        if (StringUtils.isBlank(dayOfYear)) {
            throw new IllegalSettingsException(dayOfYear);
        }
        String[] date = dayOfYear.split("\\.");
        if (StringUtils.isBlank(channelId) ||
                StringUtils.isBlank(messageId) ||
                date.length != 2 ||
                !date[0].chars().allMatch(Character::isDigit) ||
                !date[1].chars().allMatch(Character::isDigit) ||
                !channelId.chars().allMatch(Character::isDigit) ||
                !messageId.chars().allMatch(Character::isDigit)) {
            event.reply("Wystąpił nieoczekiwany błąd.").setEphemeral(true).queue();
            throw new IllegalSettingsException("Settings - is not digit");
        }
    }

    private void drawAndShow(SlashCommandInteractionEvent event) {
        List<Member> members = RangersGuild.getClanMembers();
        if (members.size() == 0) {
            event.reply("Losowanie niemożliwe").setEphemeral(true).queue();
            log.info("Members size: 0");
            return;
        }
        Random random = new Random();
        int randomInt = random.nextInt(members.size());
        Member member = members.get(randomInt);
        log.info("Members size={}, randomInt={}, {}", members.size(), randomInt, member);
        event.reply("Rangerem dnia zostaje " + member.getAsMention()).queue(hook -> {
                    settingsService.save(SettingsKey.RANGER_OF_THE_DAY_MESSAGE, hook.getInteraction().getId());
            settingsService.save(
                    SettingsKey.RANGER_OF_THE_DAY_CHANNEL,
                    Objects.requireNonNull(hook.getInteraction().getChannelId()));
        });
        settingsService.save(SettingsKey.RANGER_OF_THE_DAY, LocalDateTime.now().getDayOfYear() + "." + LocalDateTime.now().getYear());
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SlashCommands.RANGER_OF_THE_DAY.getName(), SlashCommands.RANGER_OF_THE_DAY.getDescription()));
    }
}

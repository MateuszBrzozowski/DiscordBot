package pl.mbrzozowski.ranger.recruit;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.repository.main.RecruitBlackListRepository;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.*;

@Service
@RequiredArgsConstructor
public class RecruitBlackListService {

    private final RecruitBlackListRepository recruitBlackListRepository;

    private void save(RecruitBlackList user) {
        recruitBlackListRepository.save(user);
    }

    Optional<RecruitBlackList> findByUserId(String userId) {
        return recruitBlackListRepository.findByUserId(userId);
    }

    private void deleteByUserId(String userId) {
        recruitBlackListRepository.deleteByUserId(userId);
    }

    public void addToList(@NotNull SlashCommandInteractionEvent event) {
        String userId = Objects.requireNonNull(event.getOption("id")).getAsString();
        String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();
        String nickname = Users.getUserNicknameFromID(userId);
        RecruitBlackList recruitBlackList = new RecruitBlackList(nickname, userId, reason);
        save(recruitBlackList);
        event.reply("Użytkownik **" + nickname + "** dodany do black listy").setEphemeral(true).queue();
    }

    public void removeFromList(@NotNull SlashCommandInteractionEvent event) {
        String userId = Objects.requireNonNull(event.getOption("id")).getAsString();
        String nickname = Users.getUserNicknameFromID(userId);
        deleteByUserId(userId);
        event.reply("Użytkownik **" + nickname + "** usunięty z black listy").setEphemeral(true).queue();
    }

    public void addCommandList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(RECRUIT_BLACK_LIST_ADD, "Dodaje osobę na czarną listę. Nie będzie mogła złożyć podania")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE))
                .addOption(OptionType.STRING, "id", "User ID", true)
                .addOption(OptionType.STRING, "reason", "Powód/opis", true));
        commandData.add(Commands.slash(RECRUIT_BLACK_LIST_REMOVE, "Usuwa osobę z czarnej listy.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE))
                .addOption(OptionType.STRING, "id", "User ID", true));
        commandData.add(Commands.slash(RECRUIT_BLACK_LIST_INFO, "Wyświetla informacje o użytkowniku na liście")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE))
                .addOption(OptionType.STRING, "id", "User ID", true));
    }

    public void infoAboutUser(@NotNull SlashCommandInteractionEvent event) {
        String userId = Objects.requireNonNull(event.getOption("id")).getAsString();
        Optional<RecruitBlackList> optional = findByUserId(userId);
        if (optional.isPresent()) {
            RecruitBlackList user = optional.get();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.DARK_GRAY);
            builder.addField("Name:", user.getName(), true);
            builder.addField("User ID:", String.valueOf(user.getId()), true);
            builder.addField("Data dodatnia:", Converter.LocalDateTimeToTimestampDateTimeLongFormat(user.getDate()), false);
            builder.addField("Powód/Opis:", user.getReason(), false);
            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            return;
        }
        event.reply("Nie ma takiego użytkownika na czarnej liście").setEphemeral(true).queue();
    }
}

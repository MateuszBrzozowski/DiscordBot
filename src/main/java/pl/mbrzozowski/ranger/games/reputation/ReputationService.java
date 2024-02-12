package pl.mbrzozowski.ranger.games.reputation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.guild.ContextCommands;
import pl.mbrzozowski.ranger.guild.SlashCommands;
import pl.mbrzozowski.ranger.model.ContextCommand;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.repository.main.ReputationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationService implements SlashCommand, ContextCommand {

    private final ReputationRepository reputationRepository;
    private final Set<ReputationGiving> reputationGivings = new HashSet<>();

    private Optional<Reputation> findByUserId(String userId) {
        return reputationRepository.findByUserId(userId);
    }

    private void save(Reputation reputation) {
        reputationRepository.save(reputation);
    }

    public void plus(@NotNull UserContextInteractionEvent event) {
        User user = event.getUser();
        User targetUser = event.getTarget();
        int isAdded = plus(user, targetUser);
        sendReplay(event, isAdded, targetUser);
    }

    public void plus(@NotNull MessageContextInteractionEvent event) {
        User user = event.getUser();
        User targetUser = event.getTarget().getAuthor();
        int isAdded = plus(user, targetUser);
        sendReplay(event, isAdded, targetUser);
    }

    private void sendReplay(@NotNull CommandInteraction event, int isAdded, User targetUser) {
        if (isAdded <= 1) {
            event.reply("Użytkownik " + targetUser.getAsMention() + " dostał punkt reputacji!").queue();
            return;
        }
        if (isAdded == 2) {
            event.reply("Sam sobie? Oszalałeś chyba!").queue();
        } else if (isAdded == 3) {
            event.reply("Poczekaj chwilę zanim dasz pkt temu samemu użytkownikowi!").setEphemeral(true).queue();
        }
    }

    private int plus(@NotNull User user, User targetUser) {
        if (user.equals(targetUser)) {
            log.info("Equals Users {}-{}", user, targetUser);
            return 2;
        }
        ReputationGiving reputationGiving = new ReputationGiving(user.getId(), targetUser.getId(), LocalDateTime.now());
        reputationGivings.removeIf(rpg -> rpg.localDateTime().isBefore(LocalDateTime.now().minusMinutes(10)));
        if (reputationGivings.contains(reputationGiving)) {
            log.info("User gave rep within 10 min");
            return 3;
        }
        Optional<Reputation> userOptional = findByUserId(targetUser.getId());
        Reputation reputation;
        if (userOptional.isEmpty()) {
            reputation = Reputation.builder()
                    .userId(targetUser.getId())
                    .userName(targetUser.getName())
                    .points(1)
                    .build();
        } else {
            reputation = userOptional.get();
            reputation.setPoints(reputation.getPoints() + 1);
        }
        reputationGivings.add(reputationGiving);
        save(reputation);
        return 1;
    }

    public void show(@NotNull SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        Optional<Reputation> optional = findByUserId(userId);
        if (optional.isPresent()) {
            show(event, optional.get().getPoints());
        } else {
            show(event, 0);
        }
    }

    private void show(@NotNull SlashCommandInteractionEvent event, int points) {
        event.reply("### Twoje punkty reputacji: " + points).queue();
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SlashCommands.REP.getName(), SlashCommands.REP.getDescription()));
    }

    @Override
    public void getContextCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.context(Command.Type.USER, ContextCommands.REPUTATION.getName()));
        commandData.add(Commands.message(ContextCommands.REPUTATION.getName()));
    }
}

package pl.mbrzozowski.ranger.games.reputation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
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
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.guild.SlashCommands;
import pl.mbrzozowski.ranger.model.ContextCommand;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.repository.main.ReputationRepository;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationService implements SlashCommand, ContextCommand {

    private static final int TIME_AFTER_CAN_CHECK = 6;
    private final ReputationRepository reputationRepository;
    private final Set<ReputationGiving> reputationGivings = new HashSet<>();
    private LastCheck topTen;
    private final Map<User, LastCheck> users = new HashMap<>();

    private Optional<Reputation> findByUserId(String userId) {
        return reputationRepository.findByUserId(userId);
    }

    private List<Reputation> findAllOrderByPointsDesc() {
        return reputationRepository.findAllOrderByPointsDesc();
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
        if (canUserCheck(event.getUser())) {
            Optional<Reputation> optional = findByUserId(event.getUser().getId());
            if (optional.isPresent()) {
                show(event, optional.get().getPoints());
            } else {
                show(event, 0);
            }
        } else {
            LastCheck lastCheck = users.get(event.getUser());
            event.reply("Sprawdzałeś ostatnio swoje punkty reputacji. Wiadomość znajdziesz tutaj: " +
                    RangersGuild.getLinkToMessage(lastCheck.getChannelId(), lastCheck.getMessageId())).setEphemeral(true).queue();
        }
    }

    private boolean canUserCheck(User user) {
        if (users.containsKey(user)) {
            LastCheck lastCheck = users.get(user);
            if (lastCheck.getDateTime().isBefore(LocalDateTime.now().minusHours(TIME_AFTER_CAN_CHECK))) {
                users.remove(user);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void show(@NotNull SlashCommandInteractionEvent event, int points) {
        event.reply("### Twoje punkty reputacji: " + points).queue(hook -> {
            LastCheck userLastCheck = new LastCheck(LocalDateTime.now(), hook.getInteraction().getChannelId(), hook.getInteraction().getId());
            User user = event.getUser();
            users.put(user, userLastCheck);
        });
    }

    public void showTopTen(@NotNull SlashCommandInteractionEvent event) {
        if (canCheckTopTen()) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.DARK_GRAY);
            builder.setDescription("# Top 10 reputacje:\n" + getUsersAsString());
            event.replyEmbeds(builder.build())
                    .queue(hook -> topTen = new LastCheck(LocalDateTime.now(), hook.getInteraction().getChannelId(), hook.getInteraction().getId()));
        } else {
            event.reply("Ktoś tu ostatnio sprawdzał top 10. Sprawdź to " +
                    RangersGuild.getLinkToMessage(topTen.getChannelId(), topTen.getMessageId())).setEphemeral(true).queue();
        }

    }

    private boolean canCheckTopTen() {
        if (topTen == null) {
            return true;
        }
        if (topTen.getDateTime() == null || topTen.getMessageId() == null || topTen.getChannelId() == null) {
            return true;
        }
        return topTen.getDateTime().isBefore(LocalDateTime.now().minusHours(TIME_AFTER_CAN_CHECK));
    }

    @NotNull
    private String getUsersAsString() {
        List<Reputation> reputations = findAllOrderByPointsDesc();
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Reputation reputation : reputations) {
            User user = RangersGuild.getUser(reputation.getUserId());
            if (user != null) {
                if (user.isBot()) {
                    continue;
                }
                if (i < 1) {
                    builder.append("## ");
                } else if (i < 3) {
                    builder.append("### ");
                }
                i++;
                builder.append(i)
                        .append(". ")
                        .append(user.getAsMention())
                        .append(" - ***")
                        .append(reputation.getPoints())
                        .append("*** pkt\n");
            }
            if (i == 10) {
                break;
            }
        }
        return builder.toString();
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SlashCommands.REP.getName(), SlashCommands.REP.getDescription()));
        commandData.add(Commands.slash(SlashCommands.TOP_REP.getName(), SlashCommands.TOP_REP.getDescription()));
    }

    @Override
    public void getContextCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.context(Command.Type.USER, ContextCommands.REPUTATION.getName()));
        commandData.add(Commands.message(ContextCommands.REPUTATION.getName()));
    }
}

package pl.mbrzozowski.ranger.games.timeout;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.games.SlashCommandGame;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.guild.SlashCommands;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RandomTimeout implements SlashCommandGame {

    private static final RandomTimeout instance = new RandomTimeout();
    private final Map<User, LastCall> users = new HashMap<>();

    private RandomTimeout() {
    }

    public static RandomTimeout getInstance() {
        return instance;
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SlashCommands.RANDOM_TIMEOUT.getName(), SlashCommands.RANDOM_TIMEOUT.getDescription()));
    }

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        if (Objects.equals(Objects.requireNonNull(event.getGuild()).getOwner(), event.getMember())) {
            event.reply("Na Ciebie to nie ma mocnych. Nawet jak bym chciał to Ci nie wlepie timeouta!")
                    .setEphemeral(true).queue();
            log.info("Server owner can not play!");
            return;
        }
        int botRolePosition = RangersGuild.getSelfRolePosition();
        int userRolePosition = RangersGuild.getRolePositionOfMember(Objects.requireNonNull(event.getMember()));
        if (userRolePosition >= botRolePosition) {
            event.reply("Nie możesz użyć komendy bo masz wyższa rolę niż ja! " +
                            "Poproś założyciela serwera by to zmienił i pobaw się z innymi xD")
                    .setEphemeral(true).queue();
            log.info("User role position is higher or equal highest role than me!");
            return;
        }
        clearMap();
        final int maxAttempt = 2;
        int amount = amountOfGamesForUser(event.getUser());
        if (amount >= maxAttempt) {
            event.reply("Wykorzystałeś swoje szanse. Spróbuj ponownie za jakiś czas").setEphemeral(true).queue();
            log.info("{} can not play", event.getUser());
            return;
        }
        Random random = new Random();
        boolean isWin = random.nextBoolean();
        if (!isWin) {
            String message;
            if (amount == maxAttempt - 1) {
                message = "Nie wygrałeś!";
            } else {
                message = "Nie wygrałeś. Graj dalej!";
            }
            event.reply(message).queue(hook -> addAttempt(event.getUser()));
            return;
        }
        Guild guild = RangersGuild.getGuild();
        if (guild == null) {
            throw new NullPointerException("Guild null");
        }
        int time = random.nextInt(1430) + 10;
        guild.timeoutFor(event.getUser(), time, TimeUnit.MINUTES).queue();
        event.reply(event.getUser().getAsMention() + " " + getMessage(time))
                .queue(hook -> addAttempt(event.getUser()));
    }

    private void addAttempt(User user) {
        LastCall lastCall;
        if (users.containsKey(user)) {
            lastCall = users.remove(user);
            lastCall.setAmount(lastCall.getAmount() + 1);
            lastCall.setDateTime(LocalDateTime.now());
        } else {
            lastCall = new LastCall(LocalDateTime.now(), 1);
        }
        users.put(user, lastCall);
    }

    private void clearMap() {
        List<User> usersToRemove = new ArrayList<>();
        users.forEach((user, lastCall) -> {
            if (lastCall.getDateTime().getDayOfYear() != LocalDateTime.now().getDayOfYear()) {
                usersToRemove.add(user);
            }
        });
        for (User user : usersToRemove) {
            users.remove(user);
            log.info("{} removed from random timeout banned list", user);
        }
    }

    private int amountOfGamesForUser(User user) {
        if (users.containsKey(user)) {
            LastCall lastCall = users.get(user);
            return lastCall.getAmount();
        }
        return 0;
    }

    @NotNull
    private String getMessage(int time) {
        if (time <= 60) {
            return "Wygrał timeout na " + time + " minut";
        }
        int hour = time / 60;
        String hoursAsString;
        if (hour == 1) {
            hoursAsString = "godzinę";
        } else if (hour == 2 || hour == 3 || hour == 4 || hour == 22 || hour == 23 || hour == 24) {
            hoursAsString = "godziny";
        } else {
            hoursAsString = "godzin";
        }
        return "Wygrał timeout na " + hour + " " + hoursAsString + " i " + time % 60 + " minut";
    }
}

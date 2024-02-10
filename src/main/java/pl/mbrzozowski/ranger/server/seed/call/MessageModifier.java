package pl.mbrzozowski.ranger.server.seed.call;

import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.guild.RangersGuild;

public class MessageModifier {

    public static void addRole(StringBuilder builder, String roleId) {
        if (roleId != null) {
            Role role = RangersGuild.getRoleById(roleId);
            if (role != null) {
                builder.append(role.getAsMention());
            }
        }
    }

    public static void addPLayersCount(@NotNull StringBuilder builder, @NotNull String message, int playerCount) {
        message = message.replaceAll(Pattern.PLAYERS.getRegex(), String.valueOf(playerCount));
        message = message.replaceAll(Pattern.PLAYERS_SEED.getRegex(), playerCount + "/40");
        builder.append(message);
    }

    private enum Pattern {
        PLAYERS("\\{players\\}"),
        PLAYERS_SEED("\\{players/seed\\}");

        private final String regex;

        Pattern(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }
    }
}

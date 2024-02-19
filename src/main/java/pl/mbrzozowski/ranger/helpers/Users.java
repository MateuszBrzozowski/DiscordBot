package pl.mbrzozowski.ranger.helpers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.guild.RangersGuild;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public class Users {

    /**
     * @param userID ID użytkownika dla którego chcemy pobrać nick
     * @return Zwraca nick z konkretnego discorda lub ogólny nick użytkownika.
     */
    public static String getUserNicknameFromID(String userID) {
        Guild guild = RangersGuild.getGuild();
        if (guild == null) {
            throw new NullPointerException("Null Guild");
        }
        Member member = guild.getMemberById(userID);
        if (member != null) {
            String nickname = member.getNickname();
            return Objects.requireNonNullElseGet(nickname, () -> getUserName(userID));
        }
        return getUserName(userID);
    }

    @Nullable
    private static String getUserName(String userID) {
        JDA jda = DiscordBot.getJda();
        User user = jda.getUserById(userID);
        if (user != null) {
            String globalName = user.getGlobalName();
            return Objects.requireNonNullElseGet(globalName, user::getName);
        }
        return null;
    }

    /**
     * @param userID ID użytkownika którego chcemy sprawdzić
     * @param roleID ID roli którą sprawdzamy czy użytkownik posiada
     * @return Zwraca true jeśli użytkownik o ID userID ma role o ID roleID. W innym przypadku zwraca false.
     */
    public static boolean hasUserRole(String userID, String roleID) {
        Guild guild = RangersGuild.getGuild();
        if (guild != null) {
            Member memberById = guild.getMemberById(userID);
            if (memberById != null) {
                List<Role> userRoles = memberById.getRoles();
                for (Role role : userRoles) {
                    if (role.getId().equalsIgnoreCase(roleID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy czy jest twórcą bota
     * @return Zwraca true jeżeli użytkownik to twórca bota, W innym przypadku zwraca false.
     */
    public static boolean isDev(@NotNull String userID) {
        return userID.equalsIgnoreCase(RoleID.DEV_ID);
    }

    public static boolean memberOnGuildShorterThan(String userId, int minutes) {
        Guild guild = RangersGuild.getGuild();
        if (guild == null) {
            return false;
        }
        Member member = guild.getMemberById(userId);
        if (member == null) {
            return false;
        }
        LocalDateTime dateTimeJoined = member.getTimeJoined().toLocalDateTime();
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneOffset.UTC);
        return dateTimeJoined.plusMinutes(minutes).isAfter(dateTimeNow);
    }

    public static boolean isAdmin(String userId) {
        boolean isAdmin = hasUserRole(userId, RoleID.CLAN_COUNCIL);
        if (!isAdmin) {
            isAdmin = isDev(userId);
        }
        return isAdmin;
    }

    @NotNull
    public static String getNickname(@NotNull Member member) {
        String name = member.getNickname();
        if (name == null) {
            return member.getUser().getName();
        }
        return name;
    }

    @NotNull
    public static String replaceAllIllegalCharsInName(@NotNull String name) {
        return name.replaceAll("<", "").replaceAll(">", "").replaceAll("\\?", "")
                .replaceAll("\"", "").replaceAll("\\*", "").replaceAll("\\|", "")
                .replaceAll(":", "").replaceAll("/", "").replaceAll("\\\\", "");
    }
}

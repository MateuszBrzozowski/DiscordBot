package pl.mbrzozowski.ranger.helpers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mbrzozowski.ranger.DiscordBot;

import java.util.List;

public class Users {

    /**
     * @param userID ID użytkownika dla którego chcemy pobrać nick
     * @return Zwraca nick z konkretnego discorda lub ogólny nick użytkownika.
     */
    public static @Nullable String getUserNicknameFromID(String userID) {
        JDA jda = DiscordBot.getJda();
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            if (guild.getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
                Member memberById = guild.getMemberById(userID);
                String nickname = null;
                if (memberById != null) {
                    nickname = memberById.getNickname();
                }
                if (nickname == null) {
                    User userById = jda.getUserById(userID);
                    if (userById != null) {
                        userById.getName();
                    }
                }
                return nickname;
            }
        }
        return null;
    }

    /**
     * @param userID ID użytkownika którego chcemy sprawdzić
     * @param roleID ID roli którą sprawdzamy czy użytkownik posiada
     * @return Zwraca true jeśli użytkownik o ID userID ma role o ID roleID. W innym przypadku zwraca false.
     */
    public static boolean hasUserRole(String userID, String roleID) {
        JDA jda = DiscordBot.getJda();
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            if (guild.getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
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
        }
        return false;
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy
     * @return Zwraca true jeżeli użytkownik ma rolę innego klanu. W innym przypadku zwraca false.
     */
    public static boolean hasUserRoleAnotherClan(String userID) {
        String[] clans = {RoleID.RN_ID, RoleID.HSR_ID, RoleID.PEC_ID, RoleID.PC_ID};
        for (String clanID : clans) {
            if (hasUserRole(userID, clanID)) return true;
        }
        return false;
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy czy jest twórcą bota
     * @return Zwraca true jeżeli użytkownik to twórca bota, W innym przypadku zwraca false.
     */
    public static boolean isUserDev(@NotNull String userID) {
        return userID.equalsIgnoreCase(RoleID.DEV_ID);
    }
}

package bot.event;

import helpers.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import ranger.Repository;

public class RoleEditor {

    JDA jda = Repository.getJda();

    public void addRemoveRole(String userID, String roleString) {
        Role role = getRole(roleString);
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (role != null) {
            boolean hasRole = Users.hasUserRole(userID, role.getId());
            if (!hasRole) {
                guild.addRoleToMember(UserSnowflake.fromId(userID), role).queue();
                sendConfirmation(role, userID, true);
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " - Przypisał roblę **SEED**");
            } else {
                guild.removeRoleFromMember(UserSnowflake.fromId(userID), role).queue();
                sendConfirmation(role, userID, false);
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " - Usunął roblę **SEED**");
            }
        }
    }

    private Role getRole(String roleString) {
        if (roleString.equalsIgnoreCase(Commands.TARKOV)) {
            return jda.getRoleById(RoleID.TARKOV);
        } else if (roleString.equalsIgnoreCase(Commands.VIRTUAL_REALITY)) {
            return jda.getRoleById(RoleID.VIRTUAL_REALITY);
        } else if (roleString.equalsIgnoreCase(RoleID.SEED_ID)) {
            return jda.getRoleById(RoleID.SEED_ID);
        }
        return null;
    }

    /**
     * @param role    Rola z serwera
     * @param addRole true - jesli rola jest dodawana, false - jeśli rola jest usuwana
     */
    private void sendConfirmation(Role role, String userID, boolean addRole) {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            String msg = "**Rangers Polska**: " + role.getName() + ": ";
            if (addRole) {
                msg += "Gave you the role!";
            } else {
                msg += "Took away the role!";
            }
            privateChannel.sendMessage(msg).queue();
        });
    }
}

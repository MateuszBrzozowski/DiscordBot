package bot.event.writing;

import helpers.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

public class Roles extends Proccess {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private GuildMessageReceivedEvent event;
    private String userID;
    private JDA jda = Repository.getJda();

    public Roles(GuildMessageReceivedEvent event) {
        this.event = event;
        this.userID = event.getAuthor().getId();
    }

    @Override
    public void proccessMessage(Message message) {
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TARKOV)) {
            Role roleTarkov = jda.getRoleById(RoleID.TARKOV);
            if (message.isClanMember()) {
                boolean hasRole = Users.hasUserRole(event.getAuthor().getId(), RoleID.TARKOV);
                if (!hasRole) {
                    guild.addRoleToMember(userID, roleTarkov).queue();
                    sendConfirmation(roleTarkov, true);
                } else {
                    guild.removeRoleFromMember(userID, roleTarkov).queue();
                    sendConfirmation(roleTarkov, false);
                }
            } else {
                sendMessageNoClanMember(roleTarkov);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    private void sendMessageNoClanMember(Role role) {
        event.getChannel().sendMessage("*" + Users.getUserNicknameFromID(userID) + "*, Rola niedostępna.").queue();
        RangerLogger.info(role.getName() + " niedostępna dla " + Users.getUserNicknameFromID(userID) + " --- brak roli *Clan Member*");
    }

    private void sendConfirmation(Role roleById, boolean addRole) {
        if (addRole) {
            event.getChannel().sendMessage("*" + Users.getUserNicknameFromID(userID) + "*, Przypisano rolę *" + roleById.getName() + "*").queue();
        } else {
            event.getChannel().sendMessage("*" + Users.getUserNicknameFromID(userID) + "*, Usunięto rolę *" + roleById.getName() + "*").queue();
        }
    }
}

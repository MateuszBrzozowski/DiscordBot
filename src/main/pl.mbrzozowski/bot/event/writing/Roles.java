package bot.event.writing;

import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RoleID;
import helpers.Users;
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

    public Roles(GuildMessageReceivedEvent event) {
        this.event = event;
        this.userID = event.getAuthor().getId();
    }

    @Override
    public void proccessMessage(Message message) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TARKOV)) {
            boolean hasRole = Users.hasUserRole(event.getAuthor().getId(), RoleID.TARKOV);
            Role roleById = jda.getRoleById(RoleID.TARKOV);
            if (!hasRole) {
                guild.addRoleToMember(userID, roleById).queue();
                logger.info("dodano role tarkov");
            } else {
                guild.removeRoleFromMember(userID, roleById).queue();
                logger.info("usunieto role tarkov");
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}

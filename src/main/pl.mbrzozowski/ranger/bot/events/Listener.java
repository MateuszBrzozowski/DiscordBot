package ranger.bot.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.RoleID;
import ranger.recruit.RecruitsService;

public class Listener extends ListenerAdapter {

    private final RecruitsService recruitsService;

    public Listener(RecruitsService recruitsService) {
        this.recruitsService = recruitsService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        addRole(event.getUser().getId());
        recruitsService.checkIsRecruit(event.getUser().getId());
    }

    private void addRole(String userID) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Role role = guild.getRoleById(RoleID.SQUAD);
            if (role != null) {
                guild.addRoleToMember(UserSnowflake.fromId(userID), role).queue();
            }
        }
    }
}

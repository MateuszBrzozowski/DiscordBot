package ranger.bot.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.RoleID;
import ranger.recruit.RecruitsService;

import java.util.Collection;
import java.util.EnumSet;

public class Listener extends ListenerAdapter {

    private final RecruitsService recruitsService;

    public Listener(RecruitsService recruitsService) {
        this.recruitsService = recruitsService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        addRole(event.getUser().getId(), RoleID.SQUAD);
        checkIsRecruit(event.getUser().getId());
    }

    private void checkIsRecruit(String userID) {
//        RecruitsService recruits = Repository.getRecruits();
        boolean userHasRecruitChannel = recruitsService.userHasRecruitChannel(userID);
        if (userHasRecruitChannel) {
            Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
            String channelID = recruitsService.getChannelIdByUserId(userID);
            Guild guildRangersPL = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            Member member = guildRangersPL.getMemberById(userID);
            guildRangersPL
                    .getTextChannelById(channelID)
                    .getManager()
                    .putPermissionOverride(member, permissions, null)
                    .queue();
        }
    }

    private void addRole(String userID, String roleId) {
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        Role role = guild.getRoleById(roleId);
        guild.addRoleToMember(UserSnowflake.fromId(userID), role).queue();
    }
}

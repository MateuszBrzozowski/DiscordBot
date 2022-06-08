package bot.event;

import helpers.CategoryAndChannelID;
import helpers.RoleID;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;
import recrut.Recruits;

import java.util.Collection;
import java.util.EnumSet;

public class Listener extends ListenerAdapter {
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        addRole(event.getUser().getId(), RoleID.SQUAD);
        checkIsRecruit(event.getUser().getId());
    }

    private void checkIsRecruit(String userID) {
        Recruits recruits = Repository.getRecruits();
        boolean userHasRecruitChannel = recruits.userHasRecruitChannel(userID);
        if (userHasRecruitChannel) {
            Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
            String channelID = recruits.getChannelIDFromRecruitID(userID);
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

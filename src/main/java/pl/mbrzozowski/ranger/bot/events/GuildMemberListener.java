package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.members.GuildMembersService;
import pl.mbrzozowski.ranger.recruit.RecruitsService;

@Slf4j
@Service
public class GuildMemberListener extends ListenerAdapter {

    private final RecruitsService recruitsService;
    private final GuildMembersService guildMembersService;

    public GuildMemberListener(RecruitsService recruitsService, GuildMembersService guildMembersService) {
        this.recruitsService = recruitsService;
        this.guildMembersService = guildMembersService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        log.info(event.getUser() + " - Guild member join");
        addRole(event.getUser().getId());
        recruitsService.checkIsRecruit(event.getUser().getId());
        guildMembersService.save();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        log.info(event.getUser().getName() + " - Guild member leave");
        guildMembersService.save();
    }

    private void addRole(String userID) {
        log.info("Assigning a role to a user");
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Role role = guild.getRoleById(RoleID.SQUAD);
            if (role != null) {
                guild.addRoleToMember(UserSnowflake.fromId(userID), role).queue();
                log.info("Squad role given");
            }
        }
    }
}

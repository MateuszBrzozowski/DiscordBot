package pl.mbrzozowski.ranger.members;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.repository.main.InOutGuildMembersRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
public class GuildMembersService {

    private final InOutGuildMembersRepository inOutGuildMembersRepository;

    public GuildMembersService(InOutGuildMembersRepository inOutGuildMembersRepository) {
        this.inOutGuildMembersRepository = inOutGuildMembersRepository;
    }

    public void save() {
        int memberCount = getMemberCount();
        if (memberCount == -1) {
            return;
        }
        InOutGuildMembers member = InOutGuildMembers.builder()
                .date(LocalDateTime.now())
                .members(memberCount)
                .build();
        inOutGuildMembersRepository.save(member);
        log.info("Add record to in_out_members table");
    }

    public int getMemberCount() {
        JDA jda = DiscordBot.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            return guild.getMemberCount();
        }
        return -1;
    }
}

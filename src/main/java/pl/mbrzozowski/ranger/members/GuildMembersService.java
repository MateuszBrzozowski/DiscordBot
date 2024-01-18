package pl.mbrzozowski.ranger.members;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.repository.main.InOutGuildMembersRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    public void getMemberCountCSV(MessageChannelUnion channel) {
        List<InOutGuildMembers> allRecords = inOutGuildMembersRepository.findAll();
        new File(System.getProperty("java.io.tmpdir") + "Rangerbot").mkdirs();
        String tempFilePath = System.getProperty("java.io.tmpdir") + "Rangerbot\\guildmembers.csv";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilePath));
            writer.write("id;date;members\n");
            for (InOutGuildMembers member : allRecords) {
                String line = member.getId() + ";" + member.getDate().getDayOfMonth() + "." +
                        String.format("%02d", member.getDate().getMonthValue()) + "." + member.getDate().getYear() +
                        " " + member.getDate().getHour() + ":" + String.format("%02d", member.getDate().getMinute())
                        + ";" + member.getMembers() + "\n";
                writer.write(line);
            }
            writer.close();
            log.info("Created and saved csv file ({}) with records from in_out_members table", tempFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(tempFilePath);
        FileUpload fileUpload = FileUpload.fromData(file);
        channel.sendFiles(fileUpload).queue();
        log.info("Send csv file to user ({})", channel.getName());
    }
}

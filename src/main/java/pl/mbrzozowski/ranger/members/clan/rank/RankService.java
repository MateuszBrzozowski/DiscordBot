package pl.mbrzozowski.ranger.members.clan.rank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.members.clan.ClanMember;
import pl.mbrzozowski.ranger.members.clan.ClanMemberService;
import pl.mbrzozowski.ranger.repository.main.RankRepository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankService {

    private Set<ClanMember> fileClanMembers = new HashSet<>();
    private final ClanMemberService clanMemberService;
    private final RankRepository rankRepository;
    private File file;
    private File logFile;
    String tempLogFilePath = System.getProperty("java.io.tmpdir") + "Rangerbot\\log-rank-roles.txt";
    String tempFilePath = System.getProperty("java.io.tmpdir") + "Rangerbot\\RoleRank.csv";


    @NotNull
    private List<Rank> findAll() {
        return rankRepository.findAll();
    }

    /**
     * @param event message with file *.csv
     */
    public void update(@NotNull MessageReceivedEvent event) {
        Set<ClanMember> clanMembersToChange = new HashSet<>();
        createLogFile();
        if (!downloadFile(event)) {
            return;
        }
        List<Rank> ranks = findAll();
        readFile(ranks);
        Guild guild = getGuild(event);
        compareRanks(clanMembersToChange, ranks, guild);
        changeRoles(clanMembersToChange, ranks, guild);
        sendLogFile(event);
        clearTempData();
        log.info("Update complete");
    }

    private void sendLogFile(@NotNull MessageReceivedEvent event) {
        FileUpload fileUpload = FileUpload.fromData(logFile);
        event.getMessage().replyFiles(fileUpload).queue();
    }

    private void createLogFile() {
        new File(System.getProperty("java.io.tmpdir") + "Rangerbot").mkdirs();
        logFile = new File(tempLogFilePath);
        writeToFile("Time: " + LocalDateTime.now(), false);
        writeSeparatorToLogFile();
    }


    private void changeRoles(@NotNull Set<ClanMember> clanMembersToChange, List<Rank> ranks, Guild guild) {
        writeToFile("Ustawiam role dla " + clanMembersToChange.size() + " użytkowników\n");
        for (ClanMember clanMember : clanMembersToChange) {
            Optional<String> discordIdRank = getDiscordIdRank(ranks, clanMember.getRank());
            Role role = guild.getRoleById(discordIdRank.orElse("0"));
            List<Role> rolesToAdd = new ArrayList<>();
            List<Role> rolesToRemove = getRoles(guild, ranks);
            if (role != null) {
                rolesToAdd.add(role);
                rolesToRemove.remove(role);
            }
            Member memberById = guild.getMemberById(clanMember.getDiscordId());
            if (memberById != null) {
                guild.modifyMemberRoles(memberById, rolesToAdd, rolesToRemove).queue();
                log.info("Rank role set for {}", memberById);
                writeToFile(clanMember.getNick() + " :: " + clanMember.getRank());
            }
        }
        writeSeparatorToLogFile();
    }

    @NotNull
    private Guild getGuild(@NotNull MessageReceivedEvent event) {
        Guild guild = event.getJDA().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            event.getMessage().reply("Error. Try again later or contact with Developers").queue();
            throw new NullPointerException("Guild by id RangersPLGuild(" + CategoryAndChannelID.RANGERSPL_GUILD_ID + ") is null");
        }
        return guild;
    }

    private void clearTempData() {
        fileClanMembers = new HashSet<>();
        file.delete();
        file = null;
        logFile.delete();
        logFile = null;
    }

    private void compareRanks(Set<ClanMember> clanMembersToChange, List<Rank> ranks, Guild guild) {
        List<ClanMember> clanMembers = clanMemberService.findAll();
        List<Role> rankRoles = getRoles(guild, ranks);
        for (ClanMember fileClanMember : fileClanMembers) {
            boolean isExist = false;
            for (ClanMember clanMember : clanMembers) {
                if (fileClanMember.getDiscordId().equals(clanMember.getDiscordId())) {
                    isExist = true;
                    Optional<String> discordIdRank = getDiscordIdRank(ranks, fileClanMember.getRank());
                    Member member = guild.getMemberById(clanMember.getDiscordId());
                    if (member != null) {
                        List<Role> memberRoles = member.getRoles();
                        List<Role> sameRoles = compareRoles(rankRoles, memberRoles);
                        if (sameRoles.size() != 1 ||
                                !sameRoles.get(0).getId().equals(discordIdRank.orElse(null)) ||
                                !fileClanMember.getRank().equals(clanMember.getRank())) {
                            clanMember.setRank(fileClanMember.getRank());
                            clanMemberService.save(clanMember);
                            clanMembersToChange.add(clanMember);
                            log.info("User has not same rank roles. {}, {}", fileClanMember, clanMember);
                            break;
                        } else {
                            log.info("{} - not changed", clanMember.getNick());
                        }
                    }
                }
            }
            if (!isExist) {
                ClanMember clanMember = ClanMember.builder()
                        .nick(fileClanMember.getNick())
                        .rank(fileClanMember.getRank())
                        .steamId(fileClanMember.getSteamId())
                        .discordId(fileClanMember.getDiscordId())
                        .build();
                clanMemberService.save(clanMember);
                clanMembersToChange.add(clanMember);
                writeToFile(clanMember.getNick() + " - Zapisano nowego użytkownika.");
                log.info("User not exists. Create new record {}", clanMember);
            }
        }
        if (clanMembersToChange.size() == 0) {
            writeToFile("Nie wykryto żadnych zmian");
        }
        writeSeparatorToLogFile();
    }

    /**
     * @param listLeft  of {@link Role} to compare with list right
     * @param listRight of {@link Role} to compare with list left
     * @return List of the same {@link Role}
     */
    @NotNull
    private List<Role> compareRoles(@NotNull List<Role> listLeft, List<Role> listRight) {
        List<Role> sameRoles = new ArrayList<>();
        for (Role rankRole : listLeft) {
            for (Role memberRole : listRight) {
                if (rankRole.equals(memberRole)) {
                    sameRoles.add(rankRole);
                    break;
                }
            }
        }
        return sameRoles;
    }

    @NotNull
    private List<Role> getRoles(Guild guild, @NotNull List<Rank> ranks) {
        List<Role> roles = new ArrayList<>();
        for (Rank rank : ranks) {
            if (rank.getDiscordId().isPresent()) {
                roles.add(guild.getRoleById(rank.getDiscordId().get()));
            }
        }
        return roles;
    }

    private Optional<String> getDiscordIdRank(@NotNull List<Rank> ranks, String searchRank) {
        for (Rank rank : ranks) {
            if (rank.getName().equals(searchRank)) {
                return Optional.of(rank.getDiscordId().orElse(""));
            }
        }
        return Optional.empty();
    }

    private void writeToFile(String message, boolean append) {
        if (logFile == null) {
            throw new NullPointerException("Log file is null");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, append))) {
            writer.append(message).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(String message) {
        writeToFile(message, true);
    }

    private void writeSeparatorToLogFile() {
        writeToFile("------------");
        writeToFile("");
    }

    private void readFile(List<Rank> ranks) {
        writeToFile("Odczytuje plik.");
        if (file == null) {
            throw new NullPointerException("File is null");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                setMembersFromLine(line, ranks);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeToFile("Wczytano poprawnie: " + fileClanMembers.size() + " pozycji.");
        writeSeparatorToLogFile();
        log.info("Read file. Imported data");
    }

    private void setMembersFromLine(@NotNull String line, List<Rank> ranks) {
        String[] columns = line.split(",");
        if (columns.length < 4) {
            log.info("Skip line: {}", line);
            writeToFile("Pominięto linie - " + line);
            return;
        }
        ClanMember clanMember = ClanMember.builder()
                .nick(columns[0])
                .rank(columns[1])
                .steamId(columns[2])
                .discordId(columns[3])
                .build();
        if (!clanMemberService.valid(clanMember, ranks)) {
            log.info("Skip line: {}", line);
            writeToFile("Pominięto linie - " + line);
            return;
        }
        fileClanMembers.add(clanMember);
    }

    private boolean downloadFile(@NotNull MessageReceivedEvent event) {
        log.info("Analyze file");
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.size() != 1) {
            event.getMessage().reply("Możliwy tylko jeden plik.").queue();
            log.info("Only one files required. Number of files:{}", attachments.size());
            return false;
        }
        String fileExtension = event.getMessage().getAttachments().get(0).getFileExtension();
        if (fileExtension == null || !fileExtension.equals("csv")) {
            event.getMessage().reply("Nieprawidłowe rozszerzenie pliku.").queue();
            log.info("Extension of file incorrect. File{}", event.getMessage().getAttachments().get(0).getFileName());
            return false;
        }
        log.info("Extension *.csv file accepted");
        AttachmentProxy attachmentProxy = event.getMessage().getAttachments().get(0).getProxy();
        try {
            log.info("Download file...");
            file = attachmentProxy.downloadToFile(new File(tempFilePath)).get();
            log.info("Downloaded file {}", event.getMessage().getAttachments().get(0).getFileName());
        } catch (InterruptedException | ExecutionException e) {
            event.getMessage().reply("Pobierałem, pobierałem i zgubiłem gdzieś pliki. Prześlij jeszcze raz mordzio").queue();
            log.error("Can not download file", e);
            return false;
        }
        writeToFile("Plik pobrany - " + event.getMessage().getAttachments().get(0).getFileName());
        writeSeparatorToLogFile();
        return true;
    }
}


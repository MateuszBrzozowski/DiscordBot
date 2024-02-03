package pl.mbrzozowski.ranger.members.clan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.members.clan.rank.Rank;
import pl.mbrzozowski.ranger.repository.main.ClanMemberRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClanMemberService {

    private final ClanMemberRepository clanMemberRepository;

    public void save(ClanMember clanMember) {
        clanMemberRepository.save(clanMember);
    }

    public List<ClanMember> findAll() {
        return clanMemberRepository.findAll();
    }

    public boolean valid(ClanMember clanMember, List<Rank> ranks) {
        if (clanMember == null) {
            return false;
        }
        if (StringUtils.isBlank(clanMember.getNick()) ||
                StringUtils.isBlank(clanMember.getRank()) ||
                StringUtils.isBlank(clanMember.getSteamId()) ||
                StringUtils.isBlank(clanMember.getDiscordId())) {
            return false;
        }
        if (isCorrectRank(clanMember, ranks)) return false;

        if (!clanMember.getSteamId().chars().allMatch(Character::isDigit)) {
            return false;
        }
        if (!clanMember.getDiscordId().chars().allMatch(Character::isDigit)) {
            return false;
        }
        return DiscordBot.getJda().getUserById(clanMember.getDiscordId()) != null;
    }

    private boolean isCorrectRank(ClanMember clanMember, @NotNull List<Rank> ranks) {
        boolean rankIsExist = false;
        for (Rank rank : ranks) {
            if (rank.getName().equals(clanMember.getRank())) {
                rankIsExist = true;
                break;
            }
        }
        return !rankIsExist;
    }
}

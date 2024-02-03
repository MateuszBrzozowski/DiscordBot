package pl.mbrzozowski.ranger.members.clan;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.members.clan.rank.Rank;
import pl.mbrzozowski.ranger.repository.main.ClanMemberRepository;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class ClanMemberServiceTest {

    private ClanMemberService clanMemberService;
    private JDA jda = mock(JDA.class);
    List<Rank> ranks = new ArrayList<>(List.of(new Rank(null, "Private", "PVT", "")));

    @BeforeEach
    void beforeEach() {
        ClanMemberRepository clanMemberRepository = mock(ClanMemberRepository.class);
        clanMemberService = new ClanMemberService(clanMemberRepository);
    }

    @Test
    void valid_ClanMemberNull_ReturnFalse() {
        Assertions.assertFalse(clanMemberService.valid(null, ranks));
    }

    @Test
    void valid_ReturnTrue() {
        User user = mock(User.class);
        when(jda.getUserById("123")).thenReturn(user);
        try (MockedStatic<DiscordBot> mockedStatic = mockStatic(DiscordBot.class)) {
            mockedStatic.when(DiscordBot::getJda).thenReturn(jda);
            ClanMember clanMember = ClanMember.builder()
                    .nick("nick")
                    .rank("Private")
                    .discordId("123")
                    .steamId("123")
                    .build();
            Assertions.assertTrue(clanMemberService.valid(clanMember, ranks));
        }

    }

    @Test
    void valid_RankCorrect_returnTrue() {
        User user = mock(User.class);
        when(jda.getUserById("123456789012345678")).thenReturn(user);
        try (MockedStatic<DiscordBot> mockedStatic = mockStatic(DiscordBot.class)) {
            mockedStatic.when(DiscordBot::getJda).thenReturn(jda);
            ClanMember clanMember = ClanMember.builder()
                    .nick("nick")
                    .rank("Private")
                    .steamId("123456789012345678")
                    .discordId("123456789012345678")
                    .build();
            Assertions.assertTrue(clanMemberService.valid(clanMember, ranks));
        }
    }

    @Test
    void valid_NickNull_ReturnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .rank("1")
                .steamId("123")
                .discordId("123")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_NickBlank_ReturnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick(" ")
                .rank("1")
                .steamId("123")
                .discordId("123")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_RankNull_ReturnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .steamId("123")
                .discordId("123")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_RankBlank_ReturnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .rank(" ")
                .steamId("123")
                .discordId("123")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_DiscordIdBlank_ReturnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .rank("rank")
                .steamId("123")
                .discordId(" ")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_SteamIdBlank_ReturnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .rank("rank")
                .discordId("123")
                .steamId(" ")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_RankInCorrect_returnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .rank("rank")
                .steamId("123456789012345678")
                .discordId("123456789012345678")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_SteamIdNoNumber_returnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .rank("Private")
                .steamId("123456789012345678s")
                .discordId("123456789012345678")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }

    @Test
    void valid_DiscordIdNoNumber_returnFalse() {
        ClanMember clanMember = ClanMember.builder()
                .nick("nick")
                .rank("Private")
                .steamId("123456789012345678")
                .discordId("a123456789012345678")
                .build();
        Assertions.assertFalse(clanMemberService.valid(clanMember, ranks));
    }
}
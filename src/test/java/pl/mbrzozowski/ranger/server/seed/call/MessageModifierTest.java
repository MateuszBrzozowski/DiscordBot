package pl.mbrzozowski.ranger.server.seed.call;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.guild.RangersGuild;

import static org.mockito.Mockito.*;

class MessageModifierTest {

    private StringBuilder builder;
    private Role role;
    private JDA jda;

    @BeforeEach
    void beforeEach() {
        builder = new StringBuilder();
        role = mock(Role.class);
        jda = mock(JDA.class);
    }

    @Test
    void addPLayersCount_add10() {
        MessageModifier.addPLayersCount(builder, "Players {players}", 10);
        Assertions.assertEquals("Players 10", builder.toString());
    }

    @Test
    void addPLLayersCount_NoModify() {
        MessageModifier.addPLayersCount(builder, "No Players count in this message", 10);
        Assertions.assertEquals("No Players count in this message", builder.toString());
    }

    @Test
    void addPLLayersCount_IncorrectRegex() {
        MessageModifier.addPLayersCount(builder, "Incorrect regex here {player}", 10);
        Assertions.assertEquals("Incorrect regex here {player}", builder.toString());
    }

    @Test
    void addPLLayersCount_TwoTimesPutCount() {
        MessageModifier.addPLayersCount(builder, "One Time here {players} and here {players}", 10);
        Assertions.assertEquals("One Time here 10 and here 10", builder.toString());
    }

    @Test
    void addPLayersCount_add10_ReturnContSlash40() {
        MessageModifier.addPLayersCount(builder, "Players {players/seed}", 10);
        Assertions.assertEquals("Players 10/40", builder.toString());
    }

    @Test
    void addPLayersCount_TowTimesDifferentRegex() {
        MessageModifier.addPLayersCount(builder, "Players slash 40 {players/seed} and here only players count {players}", 10);
        Assertions.assertEquals("Players slash 40 10/40 and here only players count 10", builder.toString());
    }

    @Test
    void addRole_addRoleAsMention() {
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            try (MockedStatic<DiscordBot> botMockedStatic = mockStatic(DiscordBot.class)) {
                botMockedStatic.when(DiscordBot::getJda).thenReturn(jda);
                mockedStatic.when((MockedStatic.Verification) RangersGuild.getRoleById("123")).thenReturn(role);
                when(role.getAsMention()).thenReturn("<@&123>");
                MessageModifier.addRole(builder, "123");
                Assertions.assertEquals("<@&123>", builder.toString());
            }
        }
    }

    @Test
    void addRole_RoleIdNull() {
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            try (MockedStatic<DiscordBot> botMockedStatic = mockStatic(DiscordBot.class)) {
                botMockedStatic.when(DiscordBot::getJda).thenReturn(jda);
                mockedStatic.when((MockedStatic.Verification) RangersGuild.getRoleById(null)).thenReturn(null);
                MessageModifier.addRole(builder, "123");
                Assertions.assertEquals("", builder.toString());
            }
        }
    }

    @Test
    void addRole_RoleNull() {
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            try (MockedStatic<DiscordBot> botMockedStatic = mockStatic(DiscordBot.class)) {
                botMockedStatic.when(DiscordBot::getJda).thenReturn(jda);
                mockedStatic.when((MockedStatic.Verification) RangersGuild.getRoleById("123")).thenReturn(null);
                MessageModifier.addRole(builder, "123");
                Assertions.assertEquals("", builder.toString());
            }
        }
    }

    @Test
    void addRole_GuildNull() {
        try (MockedStatic<RangersGuild> mockedStatic = Mockito.mockStatic(RangersGuild.class)) {
            try (MockedStatic<DiscordBot> botMockedStatic = mockStatic(DiscordBot.class)) {
                botMockedStatic.when(DiscordBot::getJda).thenReturn(null);
                mockedStatic.when((MockedStatic.Verification) RangersGuild.getRoleById("123")).thenReturn(null);
                MessageModifier.addRole(builder, "123");
                Assertions.assertEquals("", builder.toString());
            }
        }
    }

}
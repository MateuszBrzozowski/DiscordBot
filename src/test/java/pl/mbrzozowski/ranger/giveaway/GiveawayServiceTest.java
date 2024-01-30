package pl.mbrzozowski.ranger.giveaway;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;
import pl.mbrzozowski.ranger.repository.main.GiveawayUsersRepository;
import pl.mbrzozowski.ranger.repository.main.PrizeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class GiveawayServiceTest {

    GiveawayService giveawayService;
    GiveawayRepository giveawayRepository;
    GiveawayUsersRepository giveawayUsersRepository;
    PrizeRepository prizeRepository;

    @BeforeEach
    void beforeEach() {
        giveawayRepository = mock(GiveawayRepository.class);
        giveawayUsersRepository = mock(GiveawayUsersRepository.class);
        prizeRepository = mock(PrizeRepository.class);
        giveawayService = new GiveawayService(giveawayRepository);
    }


    @Test
    void buttonClick_GiveawayNotExist_ThrowIllegalStateException() {
        ButtonInteractionEvent event = mock(ButtonInteractionEvent.class);
        Message message = mock(Message.class);
        ReplyCallbackAction reply = mock(ReplyCallbackAction.class);
        when(event.getMessage()).thenReturn(message);
        when(message.getId()).thenReturn("123");
        when(giveawayRepository.findByMessageId("123")).thenReturn(Optional.empty());
        when(event.reply(Mockito.anyString())).thenReturn(reply);
        when(reply.setEphemeral(true)).thenReturn(reply);
        Assertions.assertThrows(IllegalStateException.class, () -> giveawayService.buttonClickSignIn(event));
    }

    @Test
    void draw_GiveawayNotExist_ThrowIllegalStateException() {
        when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.empty());
        Assertions.assertThrows(IllegalStateException.class, () -> giveawayService.draw("1"));
    }

    @Test
    void draw_PrizesListNull_ThrowIllegalStateException() {
        Giveaway giveaway = new Giveaway();
        giveaway.setGiveawayUsers(new ArrayList<>());
        when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
        Assertions.assertThrows(IllegalStateException.class, () -> giveawayService.draw("1"));
    }

    @Test
    void draw_UsersListNull_ThrowIllegalStateException() {
        Giveaway giveaway = new Giveaway();
        giveaway.setPrizes(new ArrayList<>());
        when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
        Assertions.assertThrows(IllegalStateException.class, () -> giveawayService.draw("1"));
    }

    @Test
    void draw_ListOfPrizeNull_DoesNotThrowException() {
        Giveaway giveaway = new Giveaway();
        giveaway.setPrizes(new ArrayList<>());
        giveaway.setGiveawayUsers(new ArrayList<>());
        when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
        Assertions.assertDoesNotThrow(() -> giveawayService.draw("1"));
    }

    @Test
    void draw_1Prize1User_SizeListOfWin1() {
        Giveaway giveaway = new Giveaway();
        JDA jda = mock(JDA.class);
        MessageCreateAction messageCreateAction = mock(MessageCreateAction.class);
        TextChannel textChannel = mock(TextChannel.class);
        GiveawayUser giveawayUser = mock(GiveawayUser.class);
        Prize prize = new Prize(1L, "Prize", 1, giveaway, new ArrayList<>());
        giveaway.setId(1L);
        giveaway.setChannelId("1");
        giveaway.setGiveawayUsers(new ArrayList<>(List.of(giveawayUser)));
        giveaway.setPrizes(new ArrayList<>(List.of(prize)));
        try (MockedStatic<DiscordBot> mockStatic = mockStatic(DiscordBot.class)) {
            mockStatic.when(DiscordBot::getJda).thenReturn(jda);
            when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
            when(jda.getTextChannelById(giveaway.getChannelId())).thenReturn(textChannel);
            when(textChannel.sendMessage(anyString())).thenReturn(messageCreateAction);
            giveawayService.draw("1");
            verify(giveawayUser, times(1)).setPrize(prize);
            verify(giveawayRepository).save(giveaway);
        }
    }

    @Test
    void draw_1Prize2Users_SizeListOfWin1() {
        Giveaway giveaway = new Giveaway();
        GiveawayUser giveawayUser = mock(GiveawayUser.class);
        JDA jda = mock(JDA.class);
        MessageCreateAction messageCreateAction = mock(MessageCreateAction.class);
        TextChannel textChannel = mock(TextChannel.class);
        Prize prize = new Prize(1L, "Prize", 1, giveaway, new ArrayList<>());
        giveaway.setId(1L);
        giveaway.setChannelId("1");
        giveaway.setGiveawayUsers(new ArrayList<>(List.of(giveawayUser, giveawayUser)));
        giveaway.setPrizes(new ArrayList<>(List.of(prize)));
        try (MockedStatic<DiscordBot> mockStatic = mockStatic(DiscordBot.class)) {
            mockStatic.when(DiscordBot::getJda).thenReturn(jda);
            when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
            when(jda.getTextChannelById(giveaway.getChannelId())).thenReturn(textChannel);
            when(textChannel.sendMessage(anyString())).thenReturn(messageCreateAction);
            giveawayService.draw("1");
            verify(giveawayUser, times(1)).setPrize(prize);
            verify(giveawayRepository).save(giveaway);
        }
    }

    @Test
    void draw_UserListEmpty_SizeListOfWin1() {
        Giveaway giveaway = new Giveaway();
        GiveawayUser giveawayUser = mock(GiveawayUser.class);
        Prize prize = new Prize(1L, "Prize", 1, giveaway, new ArrayList<>());
        giveaway.setId(1L);
        giveaway.setGiveawayUsers(new ArrayList<>());
        giveaway.setPrizes(new ArrayList<>(List.of(prize)));
        when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
        giveawayService.draw("1");
        verify(giveawayUser, times(0)).setPrize(prize);
    }

    @Test
    void draw_2Prizes1User_SizeListOfWin1() {
        Giveaway giveaway = new Giveaway();
        GiveawayUser giveawayUser = mock(GiveawayUser.class);
        JDA jda = mock(JDA.class);
        MessageCreateAction messageCreateAction = mock(MessageCreateAction.class);
        TextChannel textChannel = mock(TextChannel.class);
        Prize prize = new Prize(1L, "Prize", 2, giveaway, new ArrayList<>());
        giveaway.setId(1L);
        giveaway.setChannelId("1");
        giveaway.setGiveawayUsers(new ArrayList<>(List.of(giveawayUser)));
        giveaway.setPrizes(new ArrayList<>(List.of(prize)));
        try (MockedStatic<DiscordBot> mockStatic = mockStatic(DiscordBot.class)) {
            mockStatic.when(DiscordBot::getJda).thenReturn(jda);
            when(giveawayRepository.findByMessageId("1")).thenReturn(Optional.of(giveaway));
            when(jda.getTextChannelById(giveaway.getChannelId())).thenReturn(textChannel);
            when(textChannel.sendMessage(anyString())).thenReturn(messageCreateAction);
            giveawayService.draw("1");
            verify(giveawayUser, times(1)).setPrize(prize);
            verify(giveawayRepository).save(giveaway);
        }
    }
}
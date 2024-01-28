package pl.mbrzozowski.ranger.giveaway;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiveawayServiceTest {

    GiveawayService giveawayService;
    GiveawayRepository giveawayRepository;

    @BeforeEach
    void beforeEach() {
        giveawayRepository = mock(GiveawayRepository.class);
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
        Assertions.assertThrows(IllegalStateException.class, () -> giveawayService.buttonClick(event));
    }

//    @Test
//    void buttonClick_GiveawayNullEndTime_ThrowNullPointerException() {
//        ButtonInteractionEvent event = mock(ButtonInteractionEvent.class);
//        Message message = mock(Message.class);
//        ReplyCallbackAction reply = mock(ReplyCallbackAction.class);
//        Giveaway giveaway = new Giveaway();
//        MessageEmbed messageEmbed = mock(MessageEmbed.class);
//        when(event.getMessage()).thenReturn(message);
//        when(message.getId()).thenReturn("123");
//        when(giveawayRepository.findByMessageId("123")).thenReturn(Optional.of(giveaway));
//        when(event.reply(Mockito.anyString())).thenReturn(reply);
//        when(reply.setEphemeral(true)).thenReturn(reply);
//        when(message.getEmbeds()).thenReturn(List.of(messageEmbed));
//        Assertions.assertThrows(IllegalStateException.class, () -> giveawayService.buttonClick(event));
//    }
}
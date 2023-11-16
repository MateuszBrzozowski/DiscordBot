package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;

@Slf4j
@Service
public class MessageUpdate extends ListenerAdapter {

    private final EventService eventService;

    public MessageUpdate(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        log.info("Message delete event");
        eventService.deleteByMsgId(event.getMessageId());
    }

}

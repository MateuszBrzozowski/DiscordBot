package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;

@Slf4j
@Service
public class MessageUpdate extends ListenerAdapter {

    private final EventService eventService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public MessageUpdate(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        log.info("[EVENT] - Message delete event");
        eventService.deleteByMsgId(event.getMessageId());
    }

}

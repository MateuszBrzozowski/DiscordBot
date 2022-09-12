package pl.mbrzozowski.ranger.bot.events;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.server.service.ServerService;

@Service
public class ChannelUpdate extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;
    private final ServerService serverService;

    public ChannelUpdate(EventService eventService, RecruitsService recruitsService, ServerService serverService) {
        this.eventService = eventService;
        this.recruitsService = recruitsService;
        this.serverService = serverService;
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        String channelID = event.getChannel().getId();
        recruitsService.deleteChannelByID(channelID);
        eventService.deleteByChannelId(channelID);
        serverService.deleteByChannelId(channelID);
    }

}

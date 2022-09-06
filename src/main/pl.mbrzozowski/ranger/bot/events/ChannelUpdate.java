package ranger.bot.events;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ranger.Repository;
import ranger.event.EventService;
import ranger.recruit.RecruitsService;
import ranger.server.service.ServerService;

@Service
public class ChannelUpdate extends ListenerAdapter {

    private final EventService eventService;
    private final RecruitsService recruitsService;

    public ChannelUpdate(EventService eventService, RecruitsService recruitsService) {
        this.eventService = eventService;
        this.recruitsService = recruitsService;
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        String channelID = event.getChannel().getId();
        recruitsService.deleteChannelByID(channelID);
        eventService.deleteByChannelId(channelID);

        //TODO
        ServerService serverService = Repository.getServerService();
        if (serverService.isChannelOnList(channelID)) {
            serverService.removeUserFromList(channelID);
        }
    }

}

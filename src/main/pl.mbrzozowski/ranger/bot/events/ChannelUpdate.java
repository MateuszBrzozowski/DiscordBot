package ranger.bot.events;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ranger.Repository;
import ranger.event.Event;
import ranger.event.EventService;
import ranger.recrut.Recruits;
import ranger.server.service.ServerService;

import java.util.Optional;

@Service
public class ChannelUpdate extends ListenerAdapter {

    private final EventService eventService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ChannelUpdate(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        Recruits recruits = Repository.getRecruits();
//        EventService events = Repository.getEvent();
        ServerService serverService = Repository.getServerService();
        String channelID = event.getChannel().getId();
        Optional<Event> eventOptional = eventService.findEventByChannelId(channelID);
        if (recruits.isRecruitChannel(channelID)) {
            recruits.deleteChannelByID(channelID);
        } else if (eventOptional.isPresent()) {
            eventService.delete(eventOptional.get());
        } else if (serverService.isChannelOnList(channelID)) {
            serverService.removeUserFromList(channelID);
        }
    }

}

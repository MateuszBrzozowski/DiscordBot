package bot.event;

import event.Event;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;
import recrut.Recruits;
import server.service.ServerService;

public class ChannelUpdate extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        Recruits recruits = Repository.getRecruits();
        Event events = Repository.getEvent();
        ServerService serverService = Repository.getServerService();
        String channelID = event.getChannel().getId();
        if (recruits.isRecruitChannel(channelID)) {
            recruits.deleteChannelByID(channelID);
        } else if (events.isActiveMatchChannelID(channelID) >= 0) {
            events.deleteChannelByID(channelID);
        } else if (serverService.isChannelOnList(channelID)) {
            serverService.removeUserFromList(channelID);
        }
    }

}

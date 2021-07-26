package bot.event;

import ranger.Repository;
import recrut.Recruits;
import event.Event;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class ChannelUpdate extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        Recruits recruits = Repository.getRecruits();
        Event events = Repository.getEvent();
        String channelID = event.getChannel().getId();
        if (recruits.isRecruitChannel(channelID)) {
            recruits.deleteChannelByID(channelID);
        } else {
            if (events.isActiveMatchChannelID(channelID) >= 0) {
                events.deleteChannelByID(channelID);
            }
        }
    }

}

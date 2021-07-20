package bot.event;

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
    private Recruits recruits;
    private Event event;

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        recruits = RangerBot.getRecruits();
        this.event = RangerBot.getEvents();
        String channelID = event.getChannel().getId();
        if (recruits.isRecruitChannel(channelID)) {
            recruits.deleteChannelByID(channelID);
        } else {
            if (this.event.isActiveMatchChannelID(channelID) >= 0) {
                this.event.deleteChannelByID(channelID);
            }
        }
    }

}

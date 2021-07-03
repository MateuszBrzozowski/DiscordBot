package events;

import model.Recruits;
import model.Event;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class ChannelUpdate extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Recruits recruits = new Recruits();
    private Event match = new Event();



    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        recruits = RangerBot.getRecruits();
        match = RangerBot.getMatches();
        String channelID = event.getChannel().getId();
        if (recruits.isRecruitChannel(channelID)){
            recruits.deleteChannelByID(channelID);
        }else{
            if (match.isActiveMatchChannelID(channelID)>=0){
                match.deleteChannelByID(channelID);
            }
        }
    }

}

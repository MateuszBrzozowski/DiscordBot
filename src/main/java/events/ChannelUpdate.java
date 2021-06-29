package events;

import embed.EmbedCloseChannel;
import embed.EmbedHelp;
import embed.EmbedNoChangeThisName;
import embed.EmbedNoChangeThisTopic;
import model.Recruits;
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


    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        recruits = RangerBot.getRecruits();
        recruits.deleteChannelByID(event.getChannel().getId());
    }

    @Override
    public void onTextChannelUpdateTopic(@NotNull TextChannelUpdateTopicEvent event) {
        recruits = RangerBot.getRecruits();
        if (recruits.isRecruitChannel(event)){
            new EmbedNoChangeThisTopic(event);
        }
    }

    @Override
    public void onTextChannelUpdateName(@NotNull TextChannelUpdateNameEvent event) {
        recruits = RangerBot.getRecruits();
        if (recruits.isRecruitChannel(event)){
            new EmbedNoChangeThisName(event);
        }
    }
}

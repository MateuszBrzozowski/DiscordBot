package events;

import model.Recruits;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class ChannelDelete extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    Recruits recruits = new Recruits();

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        recruits = RangerBot.getRecruits();
        recruits.deleteChannelByID(event.getChannel().getId());
        recruits.addAllUsersToFile();
    }
}

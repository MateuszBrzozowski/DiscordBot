package pl.mbrzozowski.ranger.guild;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.mbrzozowski.ranger.model.TemporaryChannels;

import java.util.TimerTask;

@Slf4j
@RequiredArgsConstructor
public class DeleteChannel extends TimerTask {

    private final String channelId;
    private final TemporaryChannels temporaryChannels;

    @Override
    public void run() {
        TextChannel textChannel = RangersGuild.getTextChannel(channelId);
        if (textChannel == null) {
            log.warn("Null text channel");
        }
        temporaryChannels.deleteChannelById(channelId);
    }
}

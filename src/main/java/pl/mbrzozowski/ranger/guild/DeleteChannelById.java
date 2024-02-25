package pl.mbrzozowski.ranger.guild;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.model.TemporaryChannels;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DeleteChannelById implements Consumer<String> {

    private final TemporaryChannels temporaryChannels;

    @Override
    public void accept(String channelId) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelId);
        temporaryChannels.deleteFromDBByChannelId(channelId);
        if (textChannel != null) {
            textChannel.delete().reason("Upłynął termin utrzymywania kanału").queue();
            log.info("Deleted channel by id " + channelId);
        }
    }
}

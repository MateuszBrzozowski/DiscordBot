package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.mbrzozowski.ranger.DiscordBot;

import java.util.TimerTask;

@Slf4j
public class EndGiveaway extends TimerTask {

    private final GiveawayService giveawayService;
    private final Giveaway giveaway;

    public EndGiveaway(GiveawayService giveawayService, Giveaway giveaway) {
        this.giveawayService = giveawayService;
        this.giveaway = giveaway;
    }

    @Override
    public void run() {
        int attempt = 1;
        while (attempt <= 5) {
            log.info("Request to set end of giveaway {}", giveaway);
            JDA jda = DiscordBot.getJda();
            if (jda != null) {
                TextChannel textChannel = jda.getTextChannelById(giveaway.getChannelId());
                if (textChannel != null) {
                    textChannel.retrieveMessageById(giveaway.getMessageId()).queue(giveawayService::setEndEmbed);
                    return;
                }
            }
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            attempt++;
        }
        log.warn("{} Final stage of embed can not be set", giveaway);
    }
}

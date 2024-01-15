package pl.mbrzozowski.ranger.disboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.mbrzozowski.ranger.DiscordBot;

import java.util.TimerTask;

@Slf4j
public class DisboardReminderTask extends TimerTask {

    private static final String CHANNEL_ID = "1154107337347448872";
    private final DisboardService disboardService;

    public DisboardReminderTask(DisboardService disboardService) {
        this.disboardService = disboardService;
    }

    @Override
    public void run() {
        int attempt = 1;
        while (attempt <= 5) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Request to send a bump reminder... ");
            JDA jda = DiscordBot.getJda();
            if (jda != null) {
                TextChannel channel = jda.getTextChannelById(CHANNEL_ID);
                if (channel != null) {
                    channel.sendMessage("Witaj Sir, Użyj proszę komendy **/bump** aby podbić nasz serwer.").queue();
                    disboardService.sentBumpReminder();
                    return;
                }
            }
            attempt++;
        }
    }
}

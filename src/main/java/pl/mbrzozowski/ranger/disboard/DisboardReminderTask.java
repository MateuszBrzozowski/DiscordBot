package pl.mbrzozowski.ranger.disboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.mbrzozowski.ranger.DiscordBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

@Slf4j
public class DisboardReminderTask extends TimerTask {

    static final String CHANNEL_ID = "1154107337347448872";
    private final DisboardService disboardService;
    private final List<String> reqMessages = new ArrayList<>(List.of(
            "Witaj Sir, Użyj proszę komendy **/bump** aby podbić nasz serwer.",
            "Siema mordeczki. Mógłby ktoś z was użyć komendy **/bump** aby podbić nasz serwer?",
            "Siema mordeczki. We no ktoś podbij komendą **/bump**",
            "Podbij ktoś nasz serwer komendą **/bump**. Dzięki!",
            "Mistrzuniu podbij no nasz serwer komendą **/bump**. Dzięki",
            "Szukam dobrej duszyczki co podbije nasz serwer komendą **/bump**",
            "Podbij nasz serwer komendą **/bump**",
            "Weź no użyj komendy **/bump**",
            "Podbić musze bo się uduszę. Komenda **/bump** w ten tychmiast",
            "Podbić musze bo się uduszę. Użyj komendy **/bump**",
            "Księciuniu poratujesz komendą **/bump**?",
            "Haloo!!! Prośbę mam. Podbij serwer komendą **/bump**?",
            "Podbij nasz serwer komendą **/bump**?",
            "Podbij no nasz serwer komendą **/bump**"
    ));

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
                    channel.sendMessage(getMessage()).queue(disboardService::setReqMessage);
                    disboardService.sentBumpReminder();
                    return;
                }
            }
            attempt++;
        }
    }

    private String getMessage() {
        Random random = new Random();
        int index = random.nextInt(reqMessages.size());
        return getMessage(index);
    }

    private String getMessage(int index) {
        return reqMessages.get(index);
    }
}

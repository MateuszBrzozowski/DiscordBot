package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.LocalDate.now;

@Slf4j
@Service
public class CleanerServerServiceChannel extends TimerTask implements CleanerChannel {

    private final ServerService serverService;
    private final int delayInDays;

    @Autowired
    public CleanerServerServiceChannel(ServerService serverService,
                                       @Value("${server.service.channel.cleaning}") int delay) {
        this.serverService = serverService;
        this.delayInDays = delay;
        Timer timer = new Timer();
        Date date = new Date(now().getYear() - 1900, now().getMonthValue() - 1, now().getDayOfMonth());
        date.setHours(23);
        date.setMinutes(59);
        timer.scheduleAtFixedRate(this, date, 24 * 60 * 60 * 1000);
    }

    @Override
    public void run() {
        clean();
    }

    @Override
    public void clean() {
        log.info("Server service channel cleaning");
        List<Client> clients = serverService.findAll();
        clients = clients.stream()
                .filter(client -> client.getIsClose() && client.getCloseTimestamp() != null)
                .filter(client -> client.getCloseTimestamp().isBefore(LocalDateTime.now().minusDays(delayInDays)))
                .toList();
        for (Client client : clients) {
            serverService.deleteChannelById(client.getChannelId());
        }
    }
}

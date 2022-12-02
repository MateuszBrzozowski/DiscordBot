package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final int DELAY_IN_DAYS = 2;

    @Autowired
    public CleanerServerServiceChannel(ServerService serverService) {
        this.serverService = serverService;
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
                .filter(client -> client.getCloseTimestamp().isBefore(LocalDateTime.now().minusDays(DELAY_IN_DAYS)))
                .toList();
        for (Client client : clients) {
            serverService.deleteChannelById(client.getChannelId());
        }
    }
}

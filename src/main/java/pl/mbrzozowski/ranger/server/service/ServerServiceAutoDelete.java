package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ServerServiceAutoDelete extends CleanerChannel {

    private final ServerService serverService;

    @Autowired
    public ServerServiceAutoDelete(ServerService serverService, int delay) {
        super(delay);
        this.serverService = serverService;
        log.info("Delay to delete channel for default events(days)={}", delay);
    }

    @Override
    public void run() {
        log.info("Server service channel deleting init");
        List<Client> clients = serverService.findAll();
        clients = clients.stream()
                .filter(client -> client.getIsClose() && client.getCloseTimestamp() != null)
                .filter(client -> client.getCloseTimestamp().isBefore(LocalDateTime.now().minusDays(delay)))
                .toList();
        for (Client client : clients) {
            serverService.deleteChannelById(client.getChannelId());
        }
    }
}

package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ServerServiceAutoClose extends CleanerChannel {

    private final ServerService serverService;
    private final int delay;

    @Autowired
    public ServerServiceAutoClose(ServerService serverService, int delay) {
        super(delay);
        this.serverService = serverService;
        this.delay = delay;
        log.info("Delay to auto close channels for default events(days)={}", delay);
    }

    @Override
    public void run() {
        log.info("Auto close channels server service init");
        List<Client> clients = serverService.findAll();
        clients = clients.stream()
                .filter(Client::getAutoClose)
                .filter(client -> !client.getIsClose() && client.getCloseTimestamp() == null)
                .toList();
        for (Client client : clients) {
            TextChannel textChannel = DiscordBot.getJda().getTextChannelById(client.getChannelId());
            if (textChannel == null) {
                continue;
            }
            textChannel.getHistory().retrievePast(1).queue(messages -> {
                LocalDateTime timeCreated = messages.get(0).getTimeCreated().toLocalDateTime();
                if (timeCreated.isBefore(LocalDateTime.now().minusDays(delay))) {
                    serverService.closeChannel(client, "Ranger - brak aktywności");
                }
            });
        }
    }
}

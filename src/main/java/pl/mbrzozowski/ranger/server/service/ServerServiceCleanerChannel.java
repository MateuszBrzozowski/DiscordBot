package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.model.AutoCloseChannel;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.LocalDate.now;

@Slf4j
@Service
public class ServerServiceCleanerChannel extends TimerTask implements CleanerChannel, AutoCloseChannel {

    private final ServerService serverService;
    private final int delayInDays;

    @Autowired
    public ServerServiceCleanerChannel(ServerService serverService,
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
        closeChannel();
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

    @Override
    public void closeChannel() {
        log.info("Server service auto channel closing");
        List<Client> clients = serverService.findAll();
        clients = clients.stream()
                .filter(client -> !client.getIsClose() && client.getCloseTimestamp() == null)
                .toList();
        JDA jda = DiscordBot.getJda();
        if (jda == null) {
            return;
        }
        for (Client client : clients) {
            TextChannel textChannel = jda.getTextChannelById(client.getChannelId());
            if (textChannel == null) {
                continue;
            }
            textChannel.getHistory().retrievePast(1).queue(messages -> {
                LocalDateTime timeCreated = messages.get(0).getTimeCreated().toLocalDateTime();
                if (timeCreated.isBefore(LocalDateTime.now().minusDays(3))) {
                    serverService.closeChannel(client, "Ranger **BOT**");
                }
            });
        }
    }
}

package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.model.AutoCloseChannel;
import pl.mbrzozowski.ranger.model.CleanerChannelOld;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDate.now;
import static pl.mbrzozowski.ranger.settings.SettingsKey.SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS;
import static pl.mbrzozowski.ranger.settings.SettingsKey.SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS;

@Slf4j
@Service
public class ServerServiceCleanerChannel extends TimerTask implements CleanerChannelOld, AutoCloseChannel {

    private final ServerService serverService;
    private final SettingsService settingsService;

    @Autowired
    public ServerServiceCleanerChannel(ServerService serverService,
                                       SettingsService settingsService) {
        this.serverService = serverService;
        this.settingsService = settingsService;
        checkSettings();
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
        int delayInDays = getDelayInDays(SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS);
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
                int delayInDays = getDelayInDays(SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS);
                if (timeCreated.isBefore(LocalDateTime.now().minusDays(delayInDays))) {
                    serverService.closeChannel(client, "Ranger - brak aktywności");
                }
            });
        }
    }

    private int getDelayInDays(@NotNull SettingsKey settingsKey) {
        if (!settingsKey.equals(SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS) &&
                !settingsKey.equals(SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS)) {
            throw new IllegalArgumentException("Settings key(" + settingsKey.getKey() + ") not correct");
        }
        Optional<String> optional = settingsService.find(settingsKey);
        if (optional.isPresent()) {
            try {
                int days = Integer.parseInt(optional.get());
                if (days > 0 && days < 180) {
                    log.info("Delay {} days for {}", days, settingsKey.getKey());
                    return days;
                }
                return getDefaultDelayInDay(settingsKey);
            } catch (NumberFormatException e) {
                return getDefaultDelayInDay(settingsKey);
            }
        } else {
            return getDefaultDelayInDay(settingsKey);
        }

    }

    private int getDefaultDelayInDay(@NotNull SettingsKey settingsKey) {
        switch (settingsKey) {
            case SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS -> {
                settingsService.save(SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS, 2);
                log.info("Default value: delay 2 days for {}", settingsKey.getKey());
                return 2;
            }
            case SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS -> {
                settingsService.save(SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS, 3);
                log.info("Default value: delay 3 days for {}", settingsKey.getKey());
                return 3;
            }
            default -> throw new IllegalArgumentException("Not correct setting key");
        }
    }

    private void checkSettings() {
        //Metoda niepotrzebna jak zrobię zapisywanie ustawień z poziomu prywatnej wiadomości do bota. Narazie zachować
        // żeby zapisywały przy "pierwszym" uruchomianiu i można było zmienić na serwerze
        getDelayInDays(SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS);
        getDelayInDays(SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS);
    }
}

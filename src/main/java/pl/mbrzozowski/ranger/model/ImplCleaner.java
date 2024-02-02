package pl.mbrzozowski.ranger.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventChannelsAutoDelete;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.recruit.RecruitChannelsAutoDelete;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.server.service.ServerService;
import pl.mbrzozowski.ranger.server.service.ServerServiceAutoClose;
import pl.mbrzozowski.ranger.server.service.ServerServiceAutoDelete;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.Calendar;
import java.util.Optional;
import java.util.Timer;

import static java.time.LocalDate.now;
import static pl.mbrzozowski.ranger.model.ImplCleaner.Description.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImplCleaner implements Cleaner {

    private final RecruitsService recruitsService;
    private final SettingsService settingsService;
    private final ServerService serverService;
    private final EventService eventService;

    @Override
    public void autoDeleteChannels() {
        recruit();
        event();
        server();
    }

    @Override
    public void autoCloseChannel() {
        closeServerService();
    }

    private void server() {
        int delay = getDelay(3, SettingsKey.SERVER_SERVICE_DELETE_CHANNEL_AFTER_DAYS);
        CleanerChannel serverServiceAutoDelete = new ServerServiceAutoDelete(serverService, delay);
        setTimers(SERVER, serverServiceAutoDelete, 1);
    }

    private void event() {
        int delay = getDelay(28, SettingsKey.EVENT_CHANNEL_DELETE_DELAY);
        int delayTactical = getDelay(1, SettingsKey.EVENT_CHANNEL_TACTICAL_DELETE_DELAY);
        CleanerChannel eventChannelsAutoDelete = new EventChannelsAutoDelete(eventService, delay, delayTactical);
        setTimers(EVENT, eventChannelsAutoDelete, 22);
    }

    private void recruit() {
        int delay = getDelay(5, SettingsKey.RECRUIT_CHANNEL_DELETE_DELAY);
        CleanerChannel recruitChannelsAutoDelete = new RecruitChannelsAutoDelete(recruitsService, delay);
        setTimers(RECRUIT, recruitChannelsAutoDelete, 1);
    }

    private void setTimers(@NotNull Description event, CleanerChannel cleanerChannel, int hour) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(now().getYear(), now().getMonthValue() - 1, now().getDayOfMonth(), hour, 0, 0);
        timer.scheduleAtFixedRate(cleanerChannel, calendar.getTime(), 24 * 60 * 60 * 1000);
        log.info("{} channels active: time={}:00 every 24h",
                event.getName(),
                hour);
    }

    private int getDelay(int defaultValue, SettingsKey settingsKey) {
        Optional<String> optional = settingsService.find(settingsKey);
        if (optional.isEmpty()) {
            settingsService.save(settingsKey, defaultValue);
            log.info("New settings property set - {}={}", settingsKey, defaultValue);
        }
        int delay = defaultValue;
        try {
            delay = Integer.parseInt(optional.orElse(String.valueOf(delay)));
        } catch (NumberFormatException e) {
            settingsService.save(settingsKey, delay);
            log.info("Settings property \"{}\" not correct. Set to default value={}", settingsKey.getKey(), defaultValue);
        }
        return delay;
    }

    private void closeServerService() {
        int delay = getDelay(3, SettingsKey.SERVER_SERVICE_CLOSE_CHANNEL_AFTER_DAYS);
        CleanerChannel serverServiceAutoClose = new ServerServiceAutoClose(serverService, delay);
        setTimers(SERVER_CLOSE, serverServiceAutoClose, 1);
    }

    enum Description {
        EVENT("Cleaner for event"),
        RECRUIT("Cleaner for recruit"),
        SERVER("Cleaner for server service"),
        SERVER_CLOSE("Auto close for server service");

        private final String name;

        Description(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

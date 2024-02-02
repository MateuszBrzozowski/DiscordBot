package pl.mbrzozowski.ranger.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.CleanerEventChannel;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.recruit.CleanerRecruitChannel;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.Calendar;
import java.util.Optional;
import java.util.Timer;

import static java.time.LocalDate.now;
import static pl.mbrzozowski.ranger.model.ImplCleaner.CleanerFor.EVENT;
import static pl.mbrzozowski.ranger.model.ImplCleaner.CleanerFor.RECRUIT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImplCleaner implements Cleaner {

    private final RecruitsService recruitsService;
    private final SettingsService settingsService;
    private final EventService eventService;

    @Override
    public void runCleaner() {
        recruit();
        event();
    }

    private void event() {
        int delay = getDelay(28, SettingsKey.EVENT_CHANNEL_DELETE_DELAY);
        int delayTactical = getDelay(1, SettingsKey.EVENT_CHANNEL_TACTICAL_DELETE_DELAY);
        CleanerChannel cleanerEventChannel = new CleanerEventChannel(eventService, delay, delayTactical);
        setTimers(EVENT, cleanerEventChannel, 22);
    }

    private void recruit() {
        int delay = getDelay(5, SettingsKey.RECRUIT_CHANNEL_DELETE_DELAY);
        CleanerChannel cleanerRecruitChannel = new CleanerRecruitChannel(recruitsService, delay);
        setTimers(RECRUIT,cleanerRecruitChannel,1);
    }

    private void setTimers(@NotNull CleanerFor event, CleanerChannel cleanerChannel, int hour) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(now().getYear(), now().getMonthValue() - 1, now().getDayOfMonth(), hour, 0, 0);
        timer.scheduleAtFixedRate(cleanerChannel, calendar.getTime(), 24 * 60 * 60 * 1000);
        log.info("Cleaner for {} channel active: time={}:{} every 24h",
                event.getName(),
                calendar.get(Calendar.HOUR_OF_DAY),
                String.format("%02d", calendar.get(Calendar.MINUTE)));
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

    enum CleanerFor {
        EVENT("event"),
        RECRUIT("recruit");

        private final String name;

        CleanerFor(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

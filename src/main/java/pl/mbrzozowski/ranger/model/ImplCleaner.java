package pl.mbrzozowski.ranger.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

import java.util.*;

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
    private final List<Timer> timers = new ArrayList<>();

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
        int delay = getDelay(3, SettingsKey.SERVER_SERVICE_DELETE_CHANNEL);
        CleanerChannel serverServiceAutoDelete = new ServerServiceAutoDelete(serverService, delay);
        setTimers(SERVER, serverServiceAutoDelete, 2);
    }

    private void event() {
        int delay = getDelay(28, SettingsKey.EVENT_DELETE_CHANNEL_DELAY);
        int delayTactical = getDelay(1, SettingsKey.EVENT_DELETE_CHANNEL_TACTICAL_DELAY);
        CleanerChannel eventChannelsAutoDelete = new EventChannelsAutoDelete(eventService, delay, delayTactical);
        setTimers(EVENT, eventChannelsAutoDelete, 22);
    }

    private void recruit() {
        int delay = getDelay(5, SettingsKey.RECRUIT_DELETE_CHANNEL_DELAY);
        CleanerChannel recruitChannelsAutoDelete = new RecruitChannelsAutoDelete(recruitsService, delay);
        setTimers(RECRUIT, recruitChannelsAutoDelete, 1);
    }

    private void setTimers(@NotNull Description event, CleanerChannel cleanerChannel, int hour) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(now().getYear(), now().getMonthValue() - 1, now().getDayOfMonth(), hour, 0, 0);
        timer.scheduleAtFixedRate(cleanerChannel, calendar.getTime(), 24 * 60 * 60 * 1000);
        timers.add(timer);
        log.info("{} channels set: time={}:00 every 24h",
                event.getName(),
                hour);
    }

    private int getDelay(int defaultValue, SettingsKey settingsKey) {
        Optional<String> optional = settingsService.find(settingsKey);
        if (optional.isEmpty()) {
            log.info("New settings property set - {}={}", settingsKey, defaultValue);
            settingsService.save(settingsKey, defaultValue);
        }
        int delay = defaultValue;
        try {
            delay = Integer.parseInt(optional.orElse(String.valueOf(delay)));
        } catch (NumberFormatException e) {
            log.info("Settings property \"{}\" not correct. Set to default value={}", settingsKey.getKey(), defaultValue);
            settingsService.save(settingsKey, delay);
        }
        return delay;
    }

    private void closeServerService() {
        int delay = getDelay(3, SettingsKey.SERVER_SERVICE_CLOSE_CHANNEL);
        CleanerChannel serverServiceAutoClose = new ServerServiceAutoClose(serverService, delay);
        setTimers(SERVER_CLOSE, serverServiceAutoClose, 1);
    }

    public void setDelayToDeleteChannel(@NotNull SlashCommandInteractionEvent event, SettingsKey settingsKey) {
        int days = Objects.requireNonNull(event.getOption("days")).getAsInt();
        if (days > 100 || days <= 0) {
            event.reply("**Niepoprawna wartość!**\n*0< ILOŚĆ DNI <=100*")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        settingsService.save(settingsKey, days);
        event.reply("Ustawiono " + days + " dni.").setEphemeral(true).queue();
        log.info("Set settings property - {}={}", settingsKey, days);
        resetTimers();
    }

    private void resetTimers() {
        cancelAll();
        autoDeleteChannels();
        autoCloseChannel();
    }

    private void cancelAll() {
        for (Timer timer : timers) {
            timer.cancel();
        }
        log.info("All timers canceled");
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

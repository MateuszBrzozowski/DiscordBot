package pl.mbrzozowski.ranger.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

@Slf4j
@RequiredArgsConstructor
public class AutoCheckStatsAfterDay extends TimerTask {

    private final ServerStatsService serverStatsService;

    @Override
    public void run() {
        log.info("Auto check daily stats init");
        serverStatsService.autoStatsAfterDay();
    }
}

package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.util.List;
import java.util.TimerTask;

@Slf4j
public class SeedCallExecute extends TimerTask {

    private final SeedCallService seedCallService;

    public SeedCallExecute(SeedCallService seedCallService) {
        this.seedCallService = seedCallService;
        log.info("Created seed call timer.");
    }

    @Override
    public void run() {
        log.info("Seed call check");
        List<PlayerCounts> players = seedCallService.getPlayerCountsService().findLastTwoHours();
        if (players.size() == 0) {
            log.warn("No data to check");
            return;
        }
        if (players.get(players.size() - 1).getPlayers() == 0) {
            log.debug("No players on server");
            seedCallService.resetLevels();
            return;
        }
        players.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        seedCallService.analyze(players);
    }
}

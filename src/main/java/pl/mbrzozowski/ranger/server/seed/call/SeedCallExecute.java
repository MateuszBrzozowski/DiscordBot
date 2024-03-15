package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

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
        List<PlayerCounts> players = seedCallService.getPlayerCountsService().findLastTwoHoursWhereServer(1);
        if (players.size() == 0) {
            log.warn("No data to check");
            return;
        }
        if (players.get(players.size() - 1).getPlayers() == 0) {
            log.debug("No players on server");
            seedCallService.resetLevels();
            return;
        }
        players = players.stream().filter(playerCounts -> playerCounts.getServer() == 1).collect(Collectors.toList());
        seedCallService.analyze(players);
    }
}

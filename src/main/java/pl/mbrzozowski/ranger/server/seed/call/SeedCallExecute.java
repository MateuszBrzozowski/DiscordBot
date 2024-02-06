package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;
import pl.mbrzozowski.ranger.stats.service.PlayerCountsService;

import java.util.List;
import java.util.TimerTask;

@Slf4j
public class SeedCallExecute extends TimerTask {

    private final PlayerCountsService playerCountsService;

    public SeedCallExecute(PlayerCountsService playerCountsService) {
        this.playerCountsService = playerCountsService;
    }

    @Override
    public void run() {
        log.info("Seed call init");
        List<PlayerCounts> players = playerCountsService.findLastTwoHours();
        if (players.size() == 0) {
            log.warn("No data to check");
            return;
        }
        if (players.get(players.size() - 1).getPlayers() == 0) {
            log.info("No players on server");
            return;
        }
    }
}

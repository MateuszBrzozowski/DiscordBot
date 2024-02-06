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
    }

    @Override
    public void run() {
        log.info("Seed call init");
        List<PlayerCounts> players = seedCallService.getPlayerCountsService().findLastTwoHours();
        if (players.size() == 0) {
            log.warn("No data to check");
            return;
        }
        System.out.println(players.get(players.size()-1));
        if (players.get(players.size() - 1).getPlayers() == 0) {
            log.info("No players on server");
            return;
        }
        players.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        seedCallService.checkDay();
        seedCallService.analyze(players);
    }
}

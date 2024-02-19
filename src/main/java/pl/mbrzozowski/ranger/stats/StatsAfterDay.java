package pl.mbrzozowski.ranger.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;
import pl.mbrzozowski.ranger.stats.service.*;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class StatsAfterDay {

    private final PlayerCountsService playerCountsService;
    private final RevivesService revivesService;
    private final PlayersService playersService;
    private final DeathsService deathsService;
    private final WoundsService woundsService;

    public int getPlayerCountNow() {
        Optional<PlayerCounts> last = playerCountsService.findLast();
        if (last.isPresent()) {
            log.info("Players on server: {}", last.get().getPlayers());
            return last.get().getPlayers();
        }
        log.warn("Can not found last record in DB");
        return -1;
    }
}

package pl.mbrzozowski.ranger.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;
import pl.mbrzozowski.ranger.stats.service.*;

import java.util.ArrayList;
import java.util.List;
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

    public void pullData() {
        List<PlayerCounts> activePeriod = getActivePeriod();
        if (activePeriod.isEmpty()) {
            log.info("Server empty. No data to check");
            return;
        }
        System.out.println(activePeriod.get(0));
        System.out.println(activePeriod.get(activePeriod.size() - 1));
    }

    @NotNull
    private List<PlayerCounts> getActivePeriod() {
        List<PlayerCounts> lastTwoDays = playerCountsService.findLastTwoDays();
        lastTwoDays.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        int firstIndex = 0;
        for (int i = 0; i < lastTwoDays.size(); i++) {
            if (lastTwoDays.get(i).getPlayers() > 0) {
                firstIndex = i;
                break;
            }
        }
        int lastIndex = firstIndex;
        for (int i = firstIndex; i < lastTwoDays.size(); i++) {
            if (lastTwoDays.get(i).getPlayers() == 0) {
                lastIndex = i;
            }
        }
        if (lastIndex == firstIndex) {
            return new ArrayList<>();
        }
        List<PlayerCounts> lastActiveServer = new ArrayList<>();
        for (int i = firstIndex; i <= lastIndex; i++) {
            lastActiveServer.add(lastTwoDays.get(i));
        }
        return lastActiveServer;
    }
}

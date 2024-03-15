package pl.mbrzozowski.ranger.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.PlayerCountsRepository;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerCountsService {

    private final PlayerCountsRepository playerCountsRepository;

    public List<PlayerCounts> findLastTwoHoursWhereServer(int server) {
        return playerCountsRepository.findByTimeAfterWhereServer(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(150), server);
    }

    public List<PlayerCounts> findLastDayWhereServer(int server) {
        return playerCountsRepository.findByTimeAfterWhereServer(LocalDateTime.now(ZoneOffset.UTC).minusDays(1), server);
    }

    public List<PlayerCounts> findLastTwoDaysWhereServer(int server) {
        return playerCountsRepository.findByTimeAfterWhereServer(LocalDateTime.now(ZoneOffset.UTC).minusDays(2), server);
    }

    public Optional<PlayerCounts> findLastWhereServer(int server) {
        return playerCountsRepository.findLastWhereServer(server);
    }
}

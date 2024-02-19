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

    public List<PlayerCounts> findLastTwoHours() {
        return playerCountsRepository.findByTimeAfter(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(150));
    }

    public List<PlayerCounts> findLastDay() {
        return playerCountsRepository.findByTimeAfter(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
    }

    public List<PlayerCounts> findLastTwoDays() {
        return playerCountsRepository.findByTimeAfter(LocalDateTime.now(ZoneOffset.UTC).minusDays(2));
    }

    public Optional<PlayerCounts> findLast() {
        return playerCountsRepository.findLast();
    }
}

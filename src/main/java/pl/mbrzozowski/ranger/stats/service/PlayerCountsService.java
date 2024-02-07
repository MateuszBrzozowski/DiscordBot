package pl.mbrzozowski.ranger.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.PlayerCountsRepository;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
}

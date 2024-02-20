package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.DeathsRepository;
import pl.mbrzozowski.ranger.stats.model.Deaths;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeathsService {

    private final DeathsRepository deathsRepository;

    public DeathsService(DeathsRepository deathsRepository) {
        this.deathsRepository = deathsRepository;
    }

    public List<Deaths> findByAttackerOrVictim(String attacker, String victim) {
        return deathsRepository.findByAttackerOrVictim(attacker, victim);
    }

    public List<Deaths> findByAttackerOrVictimAndTimeAfter(String attacker, LocalDateTime time) {
        return deathsRepository.findByAttackerOrVictimAndTimeAfter(attacker, time);
    }

    public List<Deaths> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return deathsRepository.findByTimeBetween(startTime, endTime);
    }
}

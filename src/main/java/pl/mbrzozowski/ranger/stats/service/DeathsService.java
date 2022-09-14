package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.DeathsRepository;
import pl.mbrzozowski.ranger.stats.model.Deaths;

import java.util.List;

@Service
public class DeathsService {

    private final DeathsRepository deathsRepository;

    public DeathsService(DeathsRepository deathsRepository) {
        this.deathsRepository = deathsRepository;
    }

    public int countByAttacker(String steamID) {
        return deathsRepository.countByAttacker(steamID);
    }

    public int countByVictim(String steamID) {
        return deathsRepository.countByVictim(steamID);
    }

    public List<Deaths> findByAttackerOrVictim(String attacker, String victim) {
        return deathsRepository.findByAttackerOrVictim(attacker,victim);
    }
}

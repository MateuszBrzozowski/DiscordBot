package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.RevivesRepository;
import pl.mbrzozowski.ranger.stats.model.Revives;

import java.util.List;

@Service
public class RevivesService {

    private final RevivesRepository revivesRepository;

    public RevivesService(RevivesRepository revivesRepository) {
        this.revivesRepository = revivesRepository;
    }

    public int countByReviver(String steamID) {
        return revivesRepository.countByReviver(steamID);
    }

    public int countByVictim(String steamID) {
        return revivesRepository.countByVictim(steamID);
    }

    public List<Revives> findByReviverOrVictim(String reviver, String victim) {
        return revivesRepository.findByReviverOrVictim(reviver, victim);
    }
}

package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.WoundsRepository;
import pl.mbrzozowski.ranger.stats.model.Wounds;

import java.util.List;

@Service
public class WoundsService {

    private final WoundsRepository woundsRepository;

    public WoundsService(WoundsRepository woundsRepository) {
        this.woundsRepository = woundsRepository;
    }

    public int countByAttacker(String steamID) {
        return woundsRepository.countByAttacker(steamID);
    }

    public int countTeamKills(String steamID) {
        return woundsRepository.countTeamKills(steamID).size();
    }

    public List<Wounds> findByAttackerOrVictim(String attacker, String victim) {
        return woundsRepository.findByAttackerOrVictim(attacker,victim);
    }
}

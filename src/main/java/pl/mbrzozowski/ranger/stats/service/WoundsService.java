package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.WoundsRepository;
import pl.mbrzozowski.ranger.stats.model.Wounds;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WoundsService {

    private final WoundsRepository woundsRepository;

    public WoundsService(WoundsRepository woundsRepository) {
        this.woundsRepository = woundsRepository;
    }

    public List<Wounds> findByAttackerOrVictim(String attacker, String victim) {
        return woundsRepository.findByAttackerOrVictim(attacker, victim);
    }

    public List<Wounds> findByAttackerOrVictimAndTimeAfter(String attacker, LocalDateTime time) {
        return woundsRepository.findByAttackerOrVictimAndTimeAfter(attacker,time);
    }
}

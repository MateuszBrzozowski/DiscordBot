package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.WoundsRepository;

@Service
public class WoundsService {

    private final WoundsRepository woundsRepository;

    public WoundsService(WoundsRepository woundsRepository) {
        this.woundsRepository = woundsRepository;
    }
}

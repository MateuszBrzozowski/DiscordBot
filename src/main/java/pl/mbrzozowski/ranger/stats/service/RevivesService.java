package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.RevivesRepository;

@Service
public class RevivesService {

    private final RevivesRepository revivesRepository;

    public RevivesService(RevivesRepository revivesRepository) {
        this.revivesRepository = revivesRepository;
    }
}

package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.DeathsRepository;

@Service
public class DeathsService {

    private final DeathsRepository deathsRepository;

    public DeathsService(DeathsRepository deathsRepository) {
        this.deathsRepository = deathsRepository;
    }
}

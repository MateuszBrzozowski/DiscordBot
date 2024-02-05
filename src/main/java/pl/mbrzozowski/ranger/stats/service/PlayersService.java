package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.PlayersRepository;
import pl.mbrzozowski.ranger.stats.model.Players;

import java.util.Optional;

@Service
public class PlayersService {

    private final PlayersRepository playersRepository;

    public PlayersService(PlayersRepository playersRepository) {
        this.playersRepository = playersRepository;
    }

    public Optional<Players> findBySteamId(String steamID) {
        return playersRepository.findById(steamID);
    }
}

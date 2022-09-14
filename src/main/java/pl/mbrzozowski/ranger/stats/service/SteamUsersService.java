package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.SteamUsersRepository;
import pl.mbrzozowski.ranger.stats.model.SteamUsers;

import java.util.Optional;

@Service
public class SteamUsersService {

    private final SteamUsersRepository steamUsersRepository;

    public SteamUsersService(SteamUsersRepository steamUsersRepository) {
        this.steamUsersRepository = steamUsersRepository;
    }

    public Optional<SteamUsers> findBySteamId(String steamID) {
        return steamUsersRepository.findById(steamID);
    }
}

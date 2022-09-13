package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.SteamUsersRepository;

@Service
public class SteamUsersService {

    private final SteamUsersRepository steamUsersRepository;

    public SteamUsersService(SteamUsersRepository steamUsersRepository) {
        this.steamUsersRepository = steamUsersRepository;
    }
}

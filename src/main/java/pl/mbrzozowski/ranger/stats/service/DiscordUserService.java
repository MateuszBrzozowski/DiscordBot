package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.DiscordUserRepository;
import pl.mbrzozowski.ranger.stats.model.DiscordUser;

import java.util.Optional;

@Service
public class DiscordUserService {

    private final DiscordUserRepository discordUserRepository;

    public DiscordUserService(DiscordUserRepository discordUserRepository) {
        this.discordUserRepository = discordUserRepository;
    }

    public void save(DiscordUser discordUser) {
        discordUserRepository.save(discordUser);
    }

    public Optional<DiscordUser> findByUserId(String userID) {
        return discordUserRepository.findById(userID);
    }
}

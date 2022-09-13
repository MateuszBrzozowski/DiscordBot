package pl.mbrzozowski.ranger.stats.service;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.stats.DiscordUserRepository;
import pl.mbrzozowski.ranger.stats.model.DiscordUser;

@Service
public class DiscordUserService {

    private final DiscordUserRepository discordUserRepository;

    public DiscordUserService(DiscordUserRepository discordUserRepository) {
        this.discordUserRepository = discordUserRepository;
    }

    public void save(DiscordUser discordUser) {
        discordUserRepository.save(discordUser);
    }
}

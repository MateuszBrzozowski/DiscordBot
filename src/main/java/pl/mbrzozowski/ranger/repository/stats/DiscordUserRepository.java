package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.DiscordUser;

@Repository
public interface DiscordUserRepository extends JpaRepository<DiscordUser, String> {
}

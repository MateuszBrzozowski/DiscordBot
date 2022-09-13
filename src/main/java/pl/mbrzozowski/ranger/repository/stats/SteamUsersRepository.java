package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.SteamUsers;

@Repository
public interface SteamUsersRepository extends JpaRepository<SteamUsers, String> {
}

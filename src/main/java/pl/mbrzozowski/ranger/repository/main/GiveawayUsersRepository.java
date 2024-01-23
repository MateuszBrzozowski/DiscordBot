package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.giveaway.GiveawayUser;

@Repository
public interface GiveawayUsersRepository extends JpaRepository<GiveawayUser, Long> {
}

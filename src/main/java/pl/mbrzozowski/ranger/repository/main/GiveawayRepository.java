package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.giveaway.Giveaway;

@Repository
public interface GiveawayRepository extends JpaRepository<Giveaway, Long> {
}

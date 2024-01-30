package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.giveaway.Giveaway;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiveawayRepository extends JpaRepository<Giveaway, Long> {
    Optional<Giveaway> findByMessageId(String messageId);

    List<Giveaway> findByIsActiveTrue();
}

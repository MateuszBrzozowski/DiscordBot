package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.games.reputation.Reputation;

import java.util.Optional;

@Repository
public interface ReputationRepository extends JpaRepository<Reputation, Long> {

    Optional<Reputation> findByUserId(String userId);
}

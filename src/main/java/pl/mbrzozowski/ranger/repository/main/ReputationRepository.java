package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.games.reputation.Reputation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReputationRepository extends JpaRepository<Reputation, Long> {

    Optional<Reputation> findByUserId(String userId);

    @Query(nativeQuery = true, value = "SELECT * FROM reputation r ORDER BY r.points DESC LIMIT 100")
    List<Reputation> findAllOrderByPointsDesc();
}

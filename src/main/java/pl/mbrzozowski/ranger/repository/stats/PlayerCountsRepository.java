package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerCountsRepository extends JpaRepository<PlayerCounts, Integer> {

    @Query(value = "SELECT p FROM dblog_playercounts p WHERE p.server = :server AND p.time > :date")
    List<PlayerCounts> findByTimeAfterWhereServer(LocalDateTime date, int server);

    @Query(value = "SELECT * FROM dblog_playercounts p WHERE p.server = :server ORDER BY p.id DESC LIMIT 1", nativeQuery = true)
    Optional<PlayerCounts> findLastWhereServer(int server);
}

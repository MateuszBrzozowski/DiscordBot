package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayerCountsRepository extends JpaRepository<PlayerCounts,Integer> {
    List<PlayerCounts> findByTimeAfter(LocalDateTime date);
}

package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Wounds;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WoundsRepository extends JpaRepository<Wounds, Integer> {

    List<Wounds> findByAttackerOrVictim(String attacker, String victim);

    @Query(value = "SELECT w FROM dblog_wounds w WHERE (attacker = :attacker OR victim = :attacker) AND time > :time")
    List<Wounds> findByAttackerOrVictimAndTimeAfter(String attacker, LocalDateTime time);

    @Query(value = "SELECT w FROM dblog_wounds w WHERE w.server = :server AND w.time > :startTime AND w.time < :endTime")
    List<Wounds> findByTimeBetweenWhereServer(LocalDateTime startTime, LocalDateTime endTime, int server);
}

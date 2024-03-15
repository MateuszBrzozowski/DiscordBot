package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Revives;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RevivesRepository extends JpaRepository<Revives, Integer> {

    List<Revives> findByReviverOrVictim(String reviver, String victim);

    @Query(value = "SELECT w FROM dblog_revives w WHERE (reviver = :reviver OR victim = :reviver) AND time > :time")
    List<Revives> findByReviverOrVictimAndTimeAfter(String reviver, LocalDateTime time);

    @Query(value = "SELECT w FROM dblog_revives w WHERE w.server = :server AND w.time > :startTime AND w.time < :endTime")
    List<Revives> findByTimeBetweenWhereServer(LocalDateTime startTime, LocalDateTime endTime, int server);
}

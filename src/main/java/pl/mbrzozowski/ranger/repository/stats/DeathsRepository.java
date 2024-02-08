package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Deaths;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeathsRepository extends JpaRepository<Deaths, Integer> {

    List<Deaths> findByAttackerOrVictim(String attacker, String victim);

    @Query("SELECT w FROM dblog_deaths w WHERE (attacker = :attacker OR victim = :attacker) AND time > :time")
    List<Deaths> findByAttackerOrVictimAndTimeAfter(String attacker, LocalDateTime time);
}

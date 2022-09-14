package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Wounds;

import java.util.List;

@Repository
public interface WoundsRepository extends JpaRepository<Wounds, Integer> {

    int countByAttacker(String steamID);

    @Query("SELECT w FROM dblog_wounds w WHERE attacker = :steamID AND teamkill=1")
    List<Wounds> countTeamKills(@Param("steamID") String steamID);

    List<Wounds> findByAttackerOrVictim(String attacker, String victim);
}

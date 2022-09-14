package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Deaths;

import java.util.List;

@Repository
public interface DeathsRepository extends JpaRepository<Deaths, Integer> {
    List<Deaths> findByAttacker(String steamID);

    int countByAttacker(String steamID);

    int countByVictim(String steamID);

    List<Deaths> findByAttackerOrVictim(String attacker, String victim);
}

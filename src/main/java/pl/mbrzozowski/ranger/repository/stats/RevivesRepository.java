package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Revives;

import java.util.List;

@Repository
public interface RevivesRepository extends JpaRepository<Revives, Integer> {
    int countByReviver(String steamID);

    int countByVictim(String steamID);

    List<Revives> findByReviverOrVictim(String reviver, String victim);
}

package pl.mbrzozowski.ranger.repository.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.stats.model.Revives;

@Repository
public interface RevivesRepository extends JpaRepository<Revives,Integer> {
}

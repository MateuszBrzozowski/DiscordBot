package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.members.clan.rank.Rank;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
}

package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.giveaway.Prize;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
}

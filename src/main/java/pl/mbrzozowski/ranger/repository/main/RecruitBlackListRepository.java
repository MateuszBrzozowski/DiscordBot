package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.recruit.RecruitBlackList;

import java.util.Optional;

@Repository
public interface RecruitBlackListRepository extends JpaRepository<RecruitBlackList, Long> {

    Optional<RecruitBlackList> findByUserId(String userID);
}

package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.mbrzozowski.ranger.recruit.RecruitBlackList;

import java.util.Optional;

@Repository
public interface RecruitBlackListRepository extends JpaRepository<RecruitBlackList, Long> {

    Optional<RecruitBlackList> findByUserId(String userId);

    @Transactional
    void deleteByUserId(String userId);
}

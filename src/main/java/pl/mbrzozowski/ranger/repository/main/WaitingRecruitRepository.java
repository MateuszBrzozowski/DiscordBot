package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.mbrzozowski.ranger.recruit.WaitingRecruit;

import java.util.Optional;

public interface WaitingRecruitRepository extends JpaRepository<WaitingRecruit, Long> {
    Optional<WaitingRecruit> findByUserId(String userId);

    @Transactional
    void deleteByUserId(String userId);
}

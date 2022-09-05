package ranger.recruit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruitRepository extends JpaRepository<Recruit, Long> {
    Optional<Recruit> findByUserId(String userId);

    Optional<Recruit> findByChannelId(String channelId);

    @Query("SELECT r FROM Recruit r WHERE r.userId=:userId AND r.channelId=null")
    Optional<Recruit> findByUserIdAndNullChannelID(String userId);
}

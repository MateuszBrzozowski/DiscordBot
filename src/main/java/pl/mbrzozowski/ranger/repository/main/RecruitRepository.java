package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.recruit.Recruit;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitRepository extends JpaRepository<Recruit, Long> {
    Optional<Recruit> findByUserId(String userId);

    Optional<Recruit> findByChannelId(String channelId);

    @Query("SELECT r FROM recruit r WHERE r.userId=:userId AND r.channelId=null")
    Optional<Recruit> findByUserIdAndNullChannelID(String userId);

    @Query("SELECT r FROM recruit r WHERE r.channelId!=null")
    List<Recruit> findAllWithChannelId();
}

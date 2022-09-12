package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.event.Event;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByMsgId(String id);

    Optional<Event> findByChannelId(String channelID);

    void deleteByMsgId(String msgId);

    void deleteByChannelId(String channelID);
}

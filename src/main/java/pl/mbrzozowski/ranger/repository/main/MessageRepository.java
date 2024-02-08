package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.mbrzozowski.ranger.server.seed.call.Levels;
import pl.mbrzozowski.ranger.server.seed.call.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByLevel(Levels level);

    @Transactional
    void deleteByLevel(Levels level);
}

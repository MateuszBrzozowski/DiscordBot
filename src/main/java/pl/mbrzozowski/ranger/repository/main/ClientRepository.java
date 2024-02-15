package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.mbrzozowski.ranger.server.service.Client;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByUserId(String userID);

    @Transactional
    void deleteByChannelId(String channelID);

    Optional<Client> findByChannelId(String channelID);

    List<Client> findByAutoCloseTrue();

    List<Client> findByIsCloseTrue();
}

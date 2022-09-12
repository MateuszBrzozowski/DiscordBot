package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.server.service.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserId(String userID);

    void deleteByChannelId(String channelID);

    Optional<Client> findByChannelId(String channelID);
}

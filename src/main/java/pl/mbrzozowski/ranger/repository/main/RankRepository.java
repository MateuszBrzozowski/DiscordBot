package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.mbrzozowski.ranger.members.clan.rank.Rank;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findByName(String name);

    Optional<Rank> findByDiscordId(String discordId);

    @Transactional
    void deleteByDiscordId(String discordId);
}

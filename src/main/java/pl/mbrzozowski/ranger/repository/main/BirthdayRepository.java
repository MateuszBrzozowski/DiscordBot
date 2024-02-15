package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.games.birthday.Birthday;

import java.util.Optional;

@Repository
public interface BirthdayRepository extends JpaRepository<Birthday, Long> {

    Optional<Birthday> findByUserId(String userId);
}

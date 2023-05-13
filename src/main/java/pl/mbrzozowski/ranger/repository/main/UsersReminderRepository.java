package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.mbrzozowski.ranger.event.reminder.UsersReminder;

import java.util.Optional;

@Repository
public interface UsersReminderRepository extends JpaRepository<UsersReminder, Long> {

    @Transactional
    void deleteByUserId(String userID);

    Optional<UsersReminder> findByUserId(String userId);
}

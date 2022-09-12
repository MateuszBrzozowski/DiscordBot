package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.event.reminder.UsersReminder;

@Repository
public interface UsersReminderRepository extends JpaRepository<UsersReminder, Long> {
}

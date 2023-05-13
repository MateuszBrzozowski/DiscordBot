package pl.mbrzozowski.ranger.event.reminder;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.main.UsersReminderRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsersReminderService {

    private final UsersReminderRepository usersReminderRepository;

    public UsersReminderService(UsersReminderRepository usersReminderRepository) {
        this.usersReminderRepository = usersReminderRepository;
    }

    public void add(String userId) {
        Optional<UsersReminder> userOptional = findByUserId(userId);
        if (userOptional.isEmpty()) {
            UsersReminder usersReminder = new UsersReminder(null, userId);
            usersReminderRepository.save(usersReminder);
        }
    }

    public void deleteByUserId(String userId) {
        usersReminderRepository.deleteByUserId(userId);
    }

    public Optional<UsersReminder> findByUserId(String userId) {
        return usersReminderRepository.findByUserId(userId);
    }

    public List<UsersReminder> findAll() {
        return usersReminderRepository.findAll();
    }
}

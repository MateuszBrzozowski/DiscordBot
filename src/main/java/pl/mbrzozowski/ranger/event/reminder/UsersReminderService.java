package pl.mbrzozowski.ranger.event.reminder;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.main.UsersReminderRepository;

import java.util.List;

@Service
public class UsersReminderService {

    private final UsersReminderRepository usersReminderRepository;

    public UsersReminderService(UsersReminderRepository usersReminderRepository) {
        this.usersReminderRepository = usersReminderRepository;
    }

    public void add(String userID) {
        UsersReminder usersReminder = new UsersReminder(null, userID);
        usersReminderRepository.save(usersReminder);
    }

    public void deleteByUserId(String userId) {
        usersReminderRepository.deleteByUserId(userId);
    }

    public List<UsersReminder> findAll() {
        return usersReminderRepository.findAll();
    }
}

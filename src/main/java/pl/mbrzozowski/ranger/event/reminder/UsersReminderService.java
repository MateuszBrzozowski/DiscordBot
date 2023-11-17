package pl.mbrzozowski.ranger.event.reminder;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.main.UsersReminderRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UsersReminderService {

    private final UsersReminderRepository usersReminderRepository;

    public UsersReminderService(UsersReminderRepository usersReminderRepository) {
        this.usersReminderRepository = usersReminderRepository;
    }

    public void add(@NotNull User user) {
        Optional<UsersReminder> userOptional = findByUserId(user.getId());
        if (userOptional.isEmpty()) {
            UsersReminder usersReminder = new UsersReminder(null, user.getId());
            usersReminderRepository.save(usersReminder);
            log.info(user + " - added to DB - reminder off");
        }
    }

    public void deleteByUserId(@NotNull User user) {
        usersReminderRepository.deleteByUserId(user.getId());
        log.info(user + " - deleted from DB - reminder on");
    }

    public Optional<UsersReminder> findByUserId(String userId) {
        return usersReminderRepository.findByUserId(userId);
    }

    public List<UsersReminder> findAll() {
        return usersReminderRepository.findAll();
    }
}

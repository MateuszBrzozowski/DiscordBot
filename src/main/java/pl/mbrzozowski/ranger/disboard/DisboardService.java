package pl.mbrzozowski.ranger.disboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.settings.SettingsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Optional;
import java.util.Timer;

import static pl.mbrzozowski.ranger.settings.SettingsKey.*;

@Slf4j
@Service
public class DisboardService {

    private final SettingsRepository settingsRepository;
    private Timer timer;
    private Message reqMessage;
    private Message disboardMessage;

    @Autowired
    public DisboardService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
        setNextReminder();
    }

    public void setNextReminder() {
        ReminderMode reminderMode = getReminderMode();
        resetCounter();
        switch (reminderMode) {
            case ONCE_A_DAY -> setNextReminder(1, 19);
            case TWICE_A_DAY -> setNextReminder(2, 18);
            case THREE_TIMES_A_DAY -> setNextReminder(3, 17);
            case EVERY_TWO_HOURS -> setNextReminder(12, 7);
            default -> log.info("Bump reminder not set");
        }
    }

    private void setNextReminder(int times, int startHour) {
        int counter = getReminderCounter();
        if (counter < times) {
            LocalDateTime lastAnswerFromDisboard = getLastDisboardAnswer();
            LocalDateTime dateTime = LocalDateTime.now().withHour(startHour).withMinute(0);
            if (dateTime.isAfter(lastAnswerFromDisboard.plusHours(2))) {
                setReminder(dateTime.getHour(), dateTime.getMinute(), false);
            } else {
                LocalDateTime lastReminderDate = getLastReminderDate();
                if (lastReminderDate.isBefore(lastAnswerFromDisboard)) {
                    setNextReminder(startHour, lastReminderDate);
                } else {
                    setNextReminder(startHour, lastAnswerFromDisboard);
                }
            }
        } else {
            LocalDateTime dateTime = LocalDateTime.now().plusDays(1L).withHour(startHour).withMinute(0);
            setReminder(dateTime.getHour(), dateTime.getMinute(), true);
        }
    }

    private void setNextReminder(int startHour, @NotNull LocalDateTime lastDate) {
        if (lastDate.plusMinutes(121).getDayOfYear() != LocalDateTime.now().getDayOfYear()) {
            LocalDateTime dateTimeNextDay = LocalDateTime.now().plusDays(1L).withHour(startHour).withMinute(0);
            setReminder(dateTimeNextDay.getHour(), dateTimeNextDay.getMinute(), true);
        } else {
            setReminder(lastDate.plusMinutes(119).getHour(), lastDate.getMinute(), false);
        }
    }

    private void setReminder(int hour, int minute, boolean nextDay) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        if (nextDay) {
            calendar.add(Calendar.DATE, 1);
        }
        if (this.timer != null) {
            this.timer.cancel();
            log.info("Bump reminder cancel");
        }
        timer.schedule(new DisboardReminderTask(this), calendar.getTime());
        this.timer = timer;
        log.info("Bump reminder set to {}.{} {}:{}", calendar.get(Calendar.DAY_OF_MONTH), String.format("%02d", calendar.get(Calendar.MONTH) + 1), hour, String.format("%02d", minute));
    }

    private ReminderMode getReminderMode() {
        Optional<String> modeOptional = settingsRepository.find(DISBOARD_REMINDER_MODE);
        if (modeOptional.isPresent()) {
            try {
                return ReminderMode.of(Integer.parseInt(modeOptional.get()));
            } catch (NumberFormatException e) {
                settingsRepository.save(DISBOARD_REMINDER_MODE, ReminderMode.DISABLE.getMode());
                return ReminderMode.DISABLE;
            }
        } else {
            settingsRepository.save(DISBOARD_REMINDER_MODE, ReminderMode.DISABLE.getMode());
            return ReminderMode.DISABLE;
        }
    }

    private int getReminderCounter() {
        Optional<String> optionalCount = settingsRepository.find(DISBOARD_REMINDER_COUNT_FOR_DAY);
        if (optionalCount.isPresent()) {
            try {
                return Integer.parseInt(optionalCount.get());
            } catch (NumberFormatException e) {
                settingsRepository.save(DISBOARD_REMINDER_COUNT_FOR_DAY, 0);
                return 0;
            }
        } else {
            settingsRepository.save(DISBOARD_REMINDER_COUNT_FOR_DAY, 0);
            return 0;
        }
    }

    @NotNull
    private LocalDateTime getLastReminderDate() {
        Optional<String> optionalDate = settingsRepository.find(DISBOARD_REMINDER_DATE);
        if (optionalDate.isPresent()) {
            try {
                return LocalDateTime.parse(optionalDate.get());
            } catch (DateTimeParseException e) {
                settingsRepository.save(DISBOARD_REMINDER_DATE, LocalDateTime.MIN.toString());
                return LocalDateTime.MIN;
            }
        } else {
            settingsRepository.save(DISBOARD_REMINDER_DATE, LocalDateTime.MIN.toString());
            return LocalDateTime.MIN;
        }
    }

    private LocalDateTime getLastDisboardAnswer() {
        Optional<String> dateOptional = settingsRepository.find(DISBOARD_REMINDER_DATE_ANSWER);
        if (dateOptional.isPresent()) {
            try {
                return LocalDateTime.parse(dateOptional.get());
            } catch (DateTimeParseException e) {
                settingsRepository.save(DISBOARD_REMINDER_DATE_ANSWER, LocalDateTime.now().minusMinutes(110).toString());
                return LocalDateTime.now().minusMinutes(110);
            }
        } else {
            settingsRepository.save(DISBOARD_REMINDER_DATE_ANSWER, LocalDateTime.now().minusMinutes(110).toString());
            return LocalDateTime.now().minusMinutes(110);
        }
    }

    private void resetCounter() {
        LocalDateTime dateTimeNow = LocalDateTime.now();
        LocalDateTime lastReminderDate = getLastReminderDate();
        if (dateTimeNow.getDayOfYear() != lastReminderDate.getDayOfYear()
                || dateTimeNow.getYear() != lastReminderDate.getYear()) {
            settingsRepository.save(DISBOARD_REMINDER_COUNT_FOR_DAY, 0);
        }
    }

    public void saveAnswerTime() {
        settingsRepository.save(DISBOARD_REMINDER_DATE_ANSWER, LocalDateTime.now().toString());
    }

    protected void sentBumpReminder() {
        settingsRepository.save(DISBOARD_REMINDER_COUNT_FOR_DAY, getReminderCounter() + 1);
        settingsRepository.save(DISBOARD_REMINDER_DATE, LocalDateTime.now().toString());
        timer = null;
        log.info("Reminder sent");
    }

    public void setReqMessage(Message reqMessage) {
        this.reqMessage = reqMessage;
    }

    public void setDisboardMessage(@NotNull Message disboardMessage) {
        if (disboardMessage.getChannelId().equalsIgnoreCase(DisboardReminderTask.CHANNEL_ID)) {
            this.disboardMessage = disboardMessage;
        }
    }

    public void planDeleteMessages() {
        DeleteMessages deleteMessages = new DeleteMessages(reqMessage, disboardMessage);
        Timer timer = new Timer();
        timer.schedule(deleteMessages, 5 * 60 * 1000); //5 minut
    }
}

package pl.mbrzozowski.ranger.disboard;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.settings.SettingsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Timer;

import static java.time.LocalDate.now;
import static pl.mbrzozowski.ranger.settings.SettingsKey.*;

@Slf4j
@Service
public class DisboardService {

    private final SettingsRepository settingsRepository;
    private final Timer[] timers = new Timer[1];

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
        LocalDateTime lastAnswerFromDisboard = getLastDisboardAnswer();
        if (counter < times) {
            LocalDateTime dateTime = LocalDateTime.now().withHour(startHour).withMinute(0);
            if (dateTime.isAfter(lastAnswerFromDisboard.plusHours(2))) {
                setReminder(dateTime.getHour(), dateTime.getMinute(), false);
            } else {
                if (lastAnswerFromDisboard.plusMinutes(121).getDayOfYear() != LocalDateTime.now().getDayOfYear()) {
                    LocalDateTime dateTimeNextDay = LocalDateTime.now().plusDays(1L).withHour(startHour).withMinute(0);
                    setReminder(dateTimeNextDay.getHour(), dateTimeNextDay.getMinute(), true);
                } else {
                    setReminder(lastAnswerFromDisboard.plusMinutes(119).getHour(), lastAnswerFromDisboard.getMinute(), false);
                }
            }
        } else {
            LocalDateTime dateTime = LocalDateTime.now().plusDays(1L).withHour(startHour).withMinute(0);
            setReminder(dateTime.getHour(), dateTime.getMinute(), true);
        }
    }

    private void setReminder(int hour, int minute, boolean nextDay) {
        Timer timer = new Timer();
        Date date = new Date(now().getYear() - 1900, now().getMonthValue() - 1, now().getDayOfMonth());
        if (nextDay) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
        }
        date.setHours(hour);
        date.setMinutes(minute);
        if (timers[0] != null) {
            timers[0].cancel();
            log.info("Bump reminder cancel");
        }
        timer.schedule(new DisboardReminderTask(this), date);
        timers[0] = timer;
        log.info("Bump reminder set to {}:{}", hour, String.format("%02d", minute));
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
        timers[0] = null;
    }
}

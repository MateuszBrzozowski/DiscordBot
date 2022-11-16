package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.LocalDate.now;

@Slf4j
@Service
public class CleanerRecruitChannel extends TimerTask implements CleanerChannel {

    private final RecruitsService recruitsService;
    private static final int DELAY_IN_DAYS = 5;

    @Autowired
    public CleanerRecruitChannel(RecruitsService recruitsService) {
        this.recruitsService = recruitsService;
        Timer timer = new Timer();
        Date date = new Date(now().getYear() - 1900, now().getMonthValue() - 1, now().getDayOfMonth());
        date.setHours(23);
        date.setMinutes(59);
        timer.scheduleAtFixedRate(this, date, 24 * 60 * 60 * 1000);
    }

    @Override
    public void clean() {
        log.info("Recruit channel cleaning");
        List<Recruit> recruits = recruitsService.findAllWithChannel();
        recruits = recruits
                .stream()
                .filter(recruit -> recruit.getRecruitmentResult() != null && recruit.getEndRecruitment() != null)
                .filter(recruit -> recruit.getEndRecruitment().isBefore(LocalDateTime.now().minusDays(DELAY_IN_DAYS)))
                .toList();
        for (Recruit recruit : recruits) {
            recruitsService.deleteChannel(recruit);
        }
    }

    @Override
    public void run() {
        clean();
    }
}

package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimerTask;

@Slf4j
public class CleanerRecruitChannel extends TimerTask {

    private final RecruitsService recruitsService;
    private final int delayInDays;

    public CleanerRecruitChannel(RecruitsService recruitsService, int delayInDays) {
        this.recruitsService = recruitsService;
        this.delayInDays = delayInDays;
    }

    @Override
    public void run() {
        log.info("Recruit channel cleaning init");
        List<Recruit> recruits = recruitsService.findAll();
        recruits = recruits
                .stream()
                .filter(recruit -> recruit.getRecruitmentResult() != null && recruit.getEndRecruitment() != null)
                .filter(recruit -> recruit.getEndRecruitment().isBefore(LocalDateTime.now().minusDays(delayInDays)))
                .toList();
        for (Recruit recruit : recruits) {
            recruitsService.deleteChannel(recruit);
        }
    }
}

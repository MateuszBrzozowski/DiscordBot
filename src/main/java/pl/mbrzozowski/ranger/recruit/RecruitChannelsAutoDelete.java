package pl.mbrzozowski.ranger.recruit;

import lombok.extern.slf4j.Slf4j;
import pl.mbrzozowski.ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class RecruitChannelsAutoDelete extends CleanerChannel {

    private final RecruitsService recruitsService;

    public RecruitChannelsAutoDelete(RecruitsService recruitsService, int delay) {
        super(delay);
        this.recruitsService = recruitsService;
        log.info("Delay to delete channel for recruitment(days)={}", delay);
    }

    @Override
    public void run() {
        log.info("Recruit channel cleaning init");
        List<Recruit> recruits = recruitsService.findAll();
        recruits = recruits
                .stream()
                .filter(recruit -> recruit.getRecruitmentResult() != null && recruit.getEndRecruitment() != null)
                .filter(recruit -> recruit.getEndRecruitment().isBefore(LocalDateTime.now().minusDays(delay)))
                .toList();
        for (Recruit recruit : recruits) {
            recruitsService.deleteChannel(recruit);
        }
    }
}

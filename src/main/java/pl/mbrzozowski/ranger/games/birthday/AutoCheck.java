package pl.mbrzozowski.ranger.games.birthday;

import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

@Slf4j
public class AutoCheck extends TimerTask {

    private final BirthdayService birthdayService;

    public AutoCheck(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Override
    public void run() {
        log.info("Birthday auto check init");
        birthdayService.start(null);
    }
}

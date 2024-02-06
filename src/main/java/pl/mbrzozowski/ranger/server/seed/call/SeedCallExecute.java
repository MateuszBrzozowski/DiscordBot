package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

@Slf4j
public class SeedCallExecute extends TimerTask {

    @Override
    public void run() {
        log.info("Seed call init");
    }
}

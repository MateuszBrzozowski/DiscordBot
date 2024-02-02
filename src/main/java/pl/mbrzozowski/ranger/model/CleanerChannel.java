package pl.mbrzozowski.ranger.model;

import java.util.TimerTask;

public abstract class CleanerChannel extends TimerTask {

    protected final int delay;

    protected CleanerChannel(int delay) {
        this.delay = delay;
    }
}

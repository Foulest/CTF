package com.readutf.inari.core.game.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class GameTask implements Runnable {

    private boolean cancelled;

    protected void cancel() {
        cancelled = true;
    }
}

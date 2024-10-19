package com.readutf.inari.core.game.spectator;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SpectatorData {

    private final long startTime = System.currentTimeMillis();
    private boolean respawn;
    private boolean canFly;
    private List<Integer> messageIntervals;
    private long durationMillis;
    private long respawnAt;

    public SpectatorData(boolean respawn, long durationMillis, boolean canFly, List<Integer> messageIntervals) {
        this.respawn = respawn;
        this.durationMillis = durationMillis;
        respawnAt = System.currentTimeMillis() + durationMillis;
        this.canFly = canFly;
        this.messageIntervals = new ArrayList<>(messageIntervals);
    }

    public void setDuration(int durationMillis) {
        this.durationMillis = durationMillis;
        respawnAt = startTime + durationMillis;
    }
}

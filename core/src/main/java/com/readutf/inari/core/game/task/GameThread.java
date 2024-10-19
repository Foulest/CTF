package com.readutf.inari.core.game.task;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.GameState;
import com.readutf.inari.core.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class GameThread extends TimerTask {

    private final Map<GameTask, TaskInfo> gameTasks;
    private final Game game;
    private final Logger logger;
    private final int startTick;

    private int lastTick;

    public GameThread(@NotNull Game game) {
        this.game = game;
        logger = game.getLoggerFactory().getLogger(GameThread.class);
        gameTasks = new HashMap<>();
        startTick = MinecraftServer.currentTick;
    }

    @Override
    public void run() {
        if (MinecraftServer.currentTick == lastTick) {
            return;
        }

        lastTick = MinecraftServer.currentTick;

        if (game == null || game.getGameState() == GameState.ENDED) {
            cancel();
            return;
        }

        int sinceFirstTick = MinecraftServer.currentTick - startTick;

        for (Map.Entry<GameTask, TaskInfo> entry : new ArrayList<>(gameTasks.entrySet())) {
            GameTask gameTask = entry.getKey();
            TaskInfo taskInfo = entry.getValue();

            if (gameTask.isCancelled()) {
                gameTasks.remove(gameTask);
                continue;
            }

            try {
                if (taskInfo.isShouldRun() || MinecraftServer.currentTick - taskInfo.getStartTick() > taskInfo.getDelay()) {
                    if (taskInfo.isRepeating()) {
                        if (MinecraftServer.currentTick - taskInfo.getStartTick() > sinceFirstTick % taskInfo.getInterval()) {
                            gameTask.run();
                        }
                    } else {
                        gameTask.run();
                        gameTasks.remove(gameTask);
                    }
                }
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                logger.debug("Error running task " + gameTask.getClass().getSimpleName() + " in game " + game.getGameId()
                        + " with delay " + taskInfo.getDelay() + " and interval " + taskInfo.getInterval());
            }
        }
    }

    public void submitRepeatingTask(GameTask GameTask, int delay, int interval) {
        gameTasks.put(GameTask, TaskInfo.repeating(delay, interval));
    }

    public void submitTask(GameTask GameTask) {
        gameTasks.put(GameTask, TaskInfo.single(0));
    }

    public void submitTask(GameTask GameTask, int interval) {
        gameTasks.put(GameTask, TaskInfo.single(interval));
    }

    @Getter
    @Setter
    static final class TaskInfo {

        @SuppressWarnings("FieldMayBeStatic")
        private final int startTick = MinecraftServer.currentTick;
        private final boolean isRepeating;
        private final int delay;
        private final int interval;
        private boolean shouldRun;

        private TaskInfo(boolean isRepeating, int delay, int interval) {
            this.isRepeating = isRepeating;
            this.delay = delay;
            this.interval = interval;
        }

        @Contract(value = "_, _ -> new", pure = true)
        static @NotNull TaskInfo repeating(int delay, int interval) {
            return new TaskInfo(true, delay, interval);
        }

        @Contract(value = "_ -> new", pure = true)
        static @NotNull TaskInfo single(int delay) {
            return new TaskInfo(false, delay, 0);
        }
    }
}

package com.readutf.inari.core.logging;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.logging.impl.GameLogger;
import com.readutf.inari.core.logging.store.LogStore;
import com.readutf.inari.core.logging.store.LogStoreFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLoggerFactory {

    @Getter
    private static final List<GameLoggerFactory> factories = new ArrayList<>();

    private final Game game;
    private final LogStoreFactory logStoreFactory;
    private final Map<String, Logger> loggers;
    private final LogStore logStore;

    public GameLoggerFactory(@NotNull Game game, @NotNull LogStoreFactory logStoreFactory) {
        factories.add(this);
        this.game = game;
        this.logStoreFactory = logStoreFactory;
        logStore = logStoreFactory.createLogStore(game.getGameId());
        loggers = new HashMap<>();
    }

    public Logger getLogger(String name) {
        return loggers.getOrDefault(name, new GameLogger(game, name, logStore));
    }

    public Logger getLogger(@NotNull Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public void shutdown() {
        logStore.shutdown();
        factories.remove(this);
    }
}

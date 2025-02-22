package com.readutf.inari.core.game;

import com.readutf.inari.core.game.exception.GameException;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    @Getter
    private static GameManager instance;

    private final Map<UUID, Game> idToGame;
    private final Map<UUID, Game> playerToGame;

    public GameManager() {
        instance = this;
        idToGame = new HashMap<>();
        playerToGame = new HashMap<>();
    }

    public void startGame(Game game) throws GameException {
        if (!Bukkit.isPrimaryThread()) {
            throw new GameException("Game must be started on the main thread.");
        }

        idToGame.put(game.getGameId(), game);

        for (UUID allPlayer : game.getAllPlayers()) {
            playerToGame.put(allPlayer, game);
        }

        game.start();
    }

    void removeGame(@NotNull Game game) {
        for (UUID allPlayer : game.getAllPlayers()) {
            playerToGame.remove(allPlayer);
        }

        idToGame.remove(game.getGameId());
    }

    public void shutdown() {
        for (Game value : idToGame.values()) {
            value.endGame(null, GameEndReason.CANCELLED);
        }
    }

    public Game getGameById(UUID uuid) {
        return idToGame.get(uuid);
    }

    public Game getGameByPlayer(UUID uuid) {
        return playerToGame.get(uuid);
    }

    public Game getGameByPlayer(@NotNull Player player) {
        return playerToGame.get(player.getUniqueId());
    }

    public Game getGameById(String gameId) {
        return getGames().stream().filter(game -> game.getGameId().toString().startsWith(gameId)).findFirst().orElse(null);
    }

    public Collection<Game> getGames() {
        return idToGame.values();
    }
}

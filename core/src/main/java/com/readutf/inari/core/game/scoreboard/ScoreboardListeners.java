package com.readutf.inari.core.game.scoreboard;

import com.readutf.inari.core.event.GameEventHandler;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.events.GameEndEvent;
import com.readutf.inari.core.game.events.GameRejoinEvent;
import com.readutf.inari.core.game.events.GameStartEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ScoreboardListeners {

    private final Game game;

    @GameEventHandler
    public void onGameJoin(GameRejoinEvent event) {
        if (game.getScoreboardProvider() != null) {
            game.getScoreboardManager().setPlayerBoard(event.getPlayer(), game.getScoreboardProvider());
        }
    }

    @GameEventHandler
    public void onGameEnd(@NotNull GameEndEvent event) {
        for (Player onlinePlayer : event.getGame().getOnlinePlayers()) {
            game.getScoreboardManager().clearPlayerBoard(onlinePlayer);
        }
    }

    @GameEventHandler
    public void onGameStart(GameStartEvent event) {
        if (game.getScoreboardProvider() != null) {
            for (Player onlinePlayer : event.getGame().getOnlinePlayers()) {
                game.getScoreboardManager().setPlayerBoard(onlinePlayer, game.getScoreboardProvider());
            }
        }
    }
}

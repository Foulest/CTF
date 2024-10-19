package com.readutf.inari.core.game.rejoin;

import com.readutf.inari.core.event.GameEventHandler;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.events.GameRejoinEvent;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class RejoinListeners {

    private final Game game;

    @GameEventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        GameRejoinEvent gameRejoinEvent = new GameRejoinEvent(event.getPlayer(), game);

        Bukkit.getPluginManager().callEvent(gameRejoinEvent);

        if (gameRejoinEvent.isCancelled()) {
            event.getPlayer().kickPlayer("You are not allowed to rejoin this game.");
        }
    }
}

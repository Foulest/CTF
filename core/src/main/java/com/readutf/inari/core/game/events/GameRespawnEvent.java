package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameRespawnEvent extends PlayerGameEvent {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    public GameRespawnEvent(Player player, Game game) {
        super(player, game);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

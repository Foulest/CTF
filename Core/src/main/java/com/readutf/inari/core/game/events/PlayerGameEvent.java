package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerGameEvent extends GameEvent{

    private final Player player;

    public PlayerGameEvent(Player player, Game game) {
        super(game);
        this.player = player;
    }
}

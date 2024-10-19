package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
abstract class PlayerGameEvent extends GameEvent {

    private final Player player;

    PlayerGameEvent(Player player, Game game) {
        super(game);
        this.player = player;
    }
}

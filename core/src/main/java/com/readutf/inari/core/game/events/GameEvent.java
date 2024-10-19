package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;

@Getter
@AllArgsConstructor
public abstract class GameEvent extends Event {

    private final Game game;
}

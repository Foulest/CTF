package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class GameRejoinEvent extends PlayerGameEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled;

    public GameRejoinEvent(Player player, Game game) {
        super(player, game);
        cancelled = false;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

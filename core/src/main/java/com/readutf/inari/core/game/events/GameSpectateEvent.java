package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.spectator.SpectatorData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class GameSpectateEvent extends PlayerGameEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private SpectatorData spectatorData;
    private boolean cancelled;

    public GameSpectateEvent(Player player, Game game, SpectatorData spectatorData) {
        super(player, game);
        this.spectatorData = spectatorData;
        cancelled = false;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

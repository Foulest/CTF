package com.readutf.inari.core.game.events;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.GameEndReason;
import com.readutf.inari.core.game.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class GameEndEvent extends GameEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final Team matchWinner;
    private final GameEndReason gameEndReason;
    private boolean cancelled;

    public GameEndEvent(Game game, Team matchWinner, GameEndReason gameEndReason) {
        super(game);
        this.matchWinner = matchWinner;
        this.gameEndReason = gameEndReason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

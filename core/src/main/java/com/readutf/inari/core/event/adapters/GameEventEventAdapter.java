package com.readutf.inari.core.event.adapters;

import com.readutf.inari.core.event.GameAdapterResult;
import com.readutf.inari.core.event.GameEventAdapter;
import com.readutf.inari.core.game.GameManager;
import com.readutf.inari.core.game.events.GameEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class GameEventEventAdapter extends GameEventAdapter {

    public GameEventEventAdapter(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public @NotNull GameAdapterResult getGame(Event event) {
        if (event instanceof GameEvent gameEvent) {
            return new GameAdapterResult(gameEvent.getGame());
        }
        return new GameAdapterResult("Event is not a GameEvent");
    }
}

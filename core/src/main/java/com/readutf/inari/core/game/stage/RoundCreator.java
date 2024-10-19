package com.readutf.inari.core.game.stage;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.exception.GameException;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RoundCreator {

    Round createRound(@NotNull Game game, Round previousRound) throws GameException;
}

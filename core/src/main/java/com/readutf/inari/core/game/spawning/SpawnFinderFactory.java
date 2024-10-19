package com.readutf.inari.core.game.spawning;

import com.readutf.inari.core.game.Game;

@FunctionalInterface
public interface SpawnFinderFactory {

    SpawnFinder create(Game game);
}

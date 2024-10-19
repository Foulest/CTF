package com.readutf.inari.core.game.spawning;

import com.readutf.inari.core.game.exception.GameException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SpawnFinder {

    @NotNull Location findSpawn(Player player) throws GameException;
}

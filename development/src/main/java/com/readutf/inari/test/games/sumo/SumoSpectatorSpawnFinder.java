package com.readutf.inari.test.games.sumo;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.spawning.SpawnFinder;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public class SumoSpectatorSpawnFinder implements SpawnFinder {

    private final Game game;

    @Override
    public @NotNull Location findSpawn(@NotNull Player player) {
        UUID lastDamager = game.getDeathManager().getLastDamager(player.getUniqueId());
        Player lastDamagerPlayer = lastDamager == null ? null : Bukkit.getPlayer(lastDamager);
        return Objects.requireNonNullElse(lastDamagerPlayer, player).getLocation().add(0, 2, 0);
    }
}

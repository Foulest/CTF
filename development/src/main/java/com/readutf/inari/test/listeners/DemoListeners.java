package com.readutf.inari.test.listeners;

import com.readutf.inari.core.game.events.GameEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DemoListeners implements Listener {

    @EventHandler
    public void onGameEnd(@NotNull GameEndEvent event) {
        for (UUID uuid : event.getGame().getAllPlayers()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Location location = new Location(Bukkit.getWorld("world"), 0, 100, 0);

                while (!location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                    location.add(0, -1, 0);
                }

                player.teleport(location);
            }
        }
    }
}

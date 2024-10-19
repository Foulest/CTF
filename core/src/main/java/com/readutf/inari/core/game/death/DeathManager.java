package com.readutf.inari.core.game.death;

import com.readutf.inari.core.event.GameEventHandler;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.events.GameDeathEvent;
import com.readutf.inari.core.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathManager {

    private final Game game;
    private final Logger logger;
    private final Map<UUID, UUID> lastDamager;

    public DeathManager(@NotNull Game game) {
        this.game = game;
        logger = game.getLoggerFactory().getLogger(DeathManager.class);
        lastDamager = new HashMap<>();
    }

    public void killPlayer(@NotNull Player player) {
        if (game.getSpectatorManager().isSpectator(player.getUniqueId())) {
            logger.debug("Failed to kill player, " + player.getName() + " is already a spectator");
            return;
        }

        if (game.getSpectatorManager().setSpectator(player.getUniqueId())) {
            logger.debug("Killing player " + player.getName());
            Bukkit.getPluginManager().callEvent(new GameDeathEvent(player, game));
        }
    }

    @GameEventHandler
    public void onDamage(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)
                || !(event.getEntity() instanceof Player damaged)) {
            return;
        }

        lastDamager.put(damaged.getUniqueId(), damager.getUniqueId());
    }

    public UUID getLastDamager(UUID playerId) {
        return lastDamager.get(playerId);
    }
}

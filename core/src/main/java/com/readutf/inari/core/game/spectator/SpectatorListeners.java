package com.readutf.inari.core.game.spectator;

import com.readutf.inari.core.event.GameEventHandler;
import com.readutf.inari.core.game.Game;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class SpectatorListeners {

    private final Game game;

    @GameEventHandler
    public void onDamageEvent(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (game.getSpectatorManager().isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @GameEventHandler
    public void onAttack(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (game.getSpectatorManager().isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

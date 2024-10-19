package com.readutf.inari.test.games.miniwalls.wither;

import com.readutf.inari.core.event.GameEventHandler;
import lombok.RequiredArgsConstructor;
import com.readutf.inari.test.games.miniwalls.MiniWallsTeam;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class WitherListeners {

    private static final long DAMAGE_COOLDOWN = 1000;

    private final WitherManager witherManager;
    private final Map<Integer, Long> lastDamage = new HashMap<>();

    @GameEventHandler
    public void onWitherDamage(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (event.getEntity() instanceof Wither wither) {
            MiniWallsTeam team = witherManager.getRound().getTeam(player.getUniqueId());

            if (witherManager.getWither(team).getEntityId() == wither.getEntityId()) {
                event.setCancelled(true);
            }

            if (System.currentTimeMillis() - lastDamage.getOrDefault(wither.getEntityId(), 0L) > DAMAGE_COOLDOWN) {
                lastDamage.put(wither.getEntityId(), System.currentTimeMillis());
                wither.setHealth(wither.getHealth() - event.getDamage());
            }
        }
    }
}

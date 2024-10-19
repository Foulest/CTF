package com.readutf.inari.test.games.shared;

import com.readutf.inari.core.event.GameEventHandler;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class BuildPreventionListeners {

    private final boolean preventBuilding;
    private final boolean preventBreaking;

    @GameEventHandler
    public void onBlockPlace(BlockBreakEvent event) {
        if (preventBuilding) {
            event.setCancelled(true);
        }
    }

    @GameEventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (preventBreaking) {
            event.setCancelled(true);
        }
    }
}

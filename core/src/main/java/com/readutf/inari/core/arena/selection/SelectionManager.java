package com.readutf.inari.core.arena.selection;

import com.readutf.inari.core.utils.WorldCuboid;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface SelectionManager {

    WorldCuboid getSelection(Player player);
}

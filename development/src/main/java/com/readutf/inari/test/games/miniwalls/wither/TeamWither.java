package com.readutf.inari.test.games.miniwalls.wither;

import com.readutf.inari.core.game.team.Team;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.jetbrains.annotations.NotNull;

@Getter
class TeamWither {

    private final Team team;
    private final int entityId;

    TeamWither(Team team, @NotNull Location location) {
        this.team = team;
        Wither wither = (Wither) location.getWorld().spawnEntity(location, EntityType.WITHER);
        entityId = wither.getEntityId();

        wither.setAI(false);
        wither.setTicksLived(600);
    }
}

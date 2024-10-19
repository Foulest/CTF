package com.readutf.inari.test.games.miniwalls.wither;

import com.readutf.inari.core.arena.marker.Marker;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.exception.GameException;
import com.readutf.inari.core.game.team.Team;
import lombok.Getter;
import com.readutf.inari.test.games.miniwalls.MiniWallsPrepRound;
import com.readutf.inari.test.games.miniwalls.MiniWallsTeam;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WitherManager {

    @Getter
    private final MiniWallsPrepRound round;
    private final Map<MiniWallsTeam, TeamWither> withers;

    public WitherManager(@NotNull Game game, MiniWallsPrepRound round) throws GameException {
        this.round = round;
        withers = new HashMap<>();

        for (Team team : game.getTeams()) {
            int teamId = game.getTeams().indexOf(team);
            Marker marker = game.getArena().getMarker("wither:" + (teamId + 1));

            if (marker == null) {
                throw new GameException("Marker not found");
            }

            Location location = marker.toLocation(game.getArena().getWorld());
            withers.put((MiniWallsTeam) team, new TeamWither(team, location));
        }

        game.registerListeners(new WitherListeners(this));
    }

    TeamWither getWither(MiniWallsTeam team) {
        return withers.get(team);
    }
}

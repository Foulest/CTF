package com.readutf.inari.test.games.ctf.flag;

import com.readutf.inari.core.arena.marker.Marker;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.exception.GameException;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.test.games.ctf.CTFRound;
import com.readutf.inari.test.games.ctf.CTFTeam;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FlagManager {

    @Getter
    private final CTFRound round;
    private final Map<CTFTeam, TeamFlag> flags;

    public FlagManager(@NotNull Game game, CTFRound round) throws GameException {
        this.round = round;
        flags = new HashMap<>();

        for (Team team : game.getTeams()) {
            int teamId = game.getTeams().indexOf(team);
            Marker flagMarker = game.getArena().getMarker("flag:" + (teamId + 1));

            if (flagMarker == null) {
                throw new GameException("Flag marker not found");
            }

            Marker returnMarker = game.getArena().getMarker("return:" + (teamId + 1));

            if (returnMarker == null) {
                throw new GameException("Return marker not found");
            }

            Location flagLocation = flagMarker.toLocation(game.getArena().getWorld());
            Location returnLocation = returnMarker.toLocation(game.getArena().getWorld());
            flags.put((CTFTeam) team, new TeamFlag(team, flagLocation, returnLocation));
        }

        game.registerListeners(new FlagListeners(this));
    }

    TeamFlag getFlag(CTFTeam team) {
        return flags.get(team);
    }
}

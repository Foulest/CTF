package com.readutf.inari.test.games.ctf;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.stage.Round;
import com.readutf.inari.core.game.team.Team;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CTFRound implements Round {

    @Getter
    private static final int maxPoints = 2;

    private final Game game;
    private final List<Location> flagLocations;
    private final List<Location> returnLocations;

    CTFRound(Game game) {
        this.game = game;
        flagLocations = getFlagMarkers(game);
        returnLocations = getReturnMarkers(game);

        System.out.println("Flag Locations: " + flagLocations);
        System.out.println("Return Locations: " + returnLocations);
    }

    @Override
    public void roundStart() {
    }

    @Override
    public void roundEnd(Team winningTeam) {
    }

    @Override
    public boolean hasRoundEnded() {
        return false;
    }

    public CTFTeam getTeam(UUID playerId) {
        return (CTFTeam) game.getTeamByPlayer(playerId);
    }

    @NotNull
    private static List<Location> getFlagMarkers(@NotNull Game game) {
        return game.getArena().getMarkers("flag:").stream().map(marker -> marker.toLocation(game.getArena().getWorld())).toList();
    }

    @NotNull
    private static List<Location> getReturnMarkers(@NotNull Game game) {
        return game.getArena().getMarkers("return:").stream().map(marker -> marker.toLocation(game.getArena().getWorld())).toList();
    }
}

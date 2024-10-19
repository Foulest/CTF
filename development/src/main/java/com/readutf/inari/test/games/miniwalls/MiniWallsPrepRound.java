package com.readutf.inari.test.games.miniwalls;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.exception.GameException;
import com.readutf.inari.core.game.stage.Round;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.utils.ColorUtils;
import com.readutf.inari.core.utils.Cuboid;
import com.readutf.inari.core.utils.Position;
import lombok.RequiredArgsConstructor;
import com.readutf.inari.test.games.miniwalls.wither.WitherManager;
import com.readutf.inari.test.utils.CancellableTask;
import com.readutf.inari.test.utils.Countdown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class MiniWallsPrepRound implements Round {

    private final Game game;
    private final List<Location> witherLocations;
    private final List<Cuboid> wallBounds;
    private final WitherManager witherManager;

    MiniWallsPrepRound(Game game) throws GameException {
        this.game = game;
        witherLocations = getWitherMarkers(game);
        wallBounds = getWallBounds(game);
        witherManager = new WitherManager(game, this);

        System.out.println("walls: " + wallBounds);
    }

    @Override
    public void roundStart() {

        Countdown.startCountdown(game, Duration.ofSeconds(15), new CancellableTask<>() {
            @Override
            public void run(Integer integer) {
                if (integer < 4) {
                    game.messageAll(ColorUtils.color("Prep phase ending in " + integer + " seconds"));
                }

                if (integer == 0) {
                    for (Cuboid wallBound : wallBounds) {
                        for (Position position : wallBound) {
                            game.getArena().getWorld().getBlockAt(position.toLocation(game.getArena().getWorld())).setType(Material.AIR);
                        }
                    }

                    game.messageAll(ColorUtils.color("Prep phase has ended"));
                }
            }
        });
    }

    @Override
    public void roundEnd(Team winnerTeam) {

    }

    @Override
    public boolean hasRoundEnded() {
        return false;
    }

    public MiniWallsTeam getTeam(UUID playerId) {
        return (MiniWallsTeam) game.getTeamByPlayer(playerId);
    }

    @NotNull
    private static List<Cuboid> getWallBounds(@NotNull Game game) {
        return Optional.ofNullable(game.getArena().getCuboids("wall")).orElse(Collections.emptyList());
    }

    @NotNull
    private static List<Location> getWitherMarkers(@NotNull Game game) {
        return game.getArena().getMarkers("wither:").stream().map(marker -> marker.toLocation(game.getArena().getWorld())).toList();
    }
}

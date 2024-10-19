package com.readutf.inari.test.games.bedwars;

import com.readutf.inari.core.event.GameEventHandler;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.events.GameSpectateEvent;
import com.readutf.inari.core.game.spectator.SpectatorData;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.utils.ColorUtils;
import com.readutf.inari.core.utils.Position;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicBedwarsListeners {

    private final Game game;
    private final BedwarsRound bedwarsRound;

    @Contract(pure = true)
    BasicBedwarsListeners(@NotNull BedwarsRound bedwarsRound) {
        this.bedwarsRound = bedwarsRound;
        game = bedwarsRound.getGame();
    }

    @GameEventHandler
    public void onDeath(@NotNull GameSpectateEvent event) {
        System.out.println("on death");

        Player player = event.getPlayer();
        BedwarsTeam team = (BedwarsTeam) event.getGame().getTeamByPlayer(player);

        if (team == null) {
            return;
        }

        System.out.println("team: " + team.getTeamName());
        System.out.println("has bed: " + team.hasBed());

        if (team.hasBed()) {
            event.setSpectatorData(new SpectatorData(true, 5000, true, List.of()));
        } else {
            event.setSpectatorData(new SpectatorData(false, 0, true, List.of()));
        }
    }

    @GameEventHandler
    public void onBreak(@NotNull BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.RED_BED) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            Team team = game.getTeamByPlayer(player);
            Team teamFromBedPosition = bedwarsRound.getTeamFromBedPosition(event.getBlock().getLocation());

            if (team == teamFromBedPosition) {
                event.setCancelled(true);
                player.sendMessage(ColorUtils.color("&cYou cannot break your own bed!"));
                return;
            }

            event.getBlock().setType(Material.AIR);

            for (BlockFace blockFace : List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
                Block relative = event.getBlock().getRelative(blockFace);

                if (relative.getType() == Material.RED_BED) {
                    relative.setType(Material.AIR);
                }
            }

            bedwarsRound.destroyBed(player, (BedwarsTeam) teamFromBedPosition);
        }
    }

    @GameEventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (!game.getArena().getBounds().contains(Position.fromLocation(event.getTo()))) {
            game.killPlayer(event.getPlayer());
        }
    }
}

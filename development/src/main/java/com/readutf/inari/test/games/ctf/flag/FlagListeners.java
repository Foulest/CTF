package com.readutf.inari.test.games.ctf.flag;

import com.readutf.inari.core.event.GameEventHandler;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.test.games.ctf.CTFRound;
import com.readutf.inari.test.games.ctf.CTFTeam;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor
public class FlagListeners {

    private final FlagManager flagManager;

    @GameEventHandler
    public void onFlagHolderDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        CTFTeam team = flagManager.getRound().getTeam(player.getUniqueId());

        if (team == null) {
            return;
        }

        // Get the enemy team
        List<Team> allTeams = flagManager.getRound().getGame().getTeams();
        CTFTeam enemyTeam = (CTFTeam) allTeams.stream().filter(t -> t != team).findFirst().orElse(null);

        // Get the flag of the enemy team
        TeamFlag enemyFlag = flagManager.getFlag(enemyTeam);

        // Find the nearest solid ground location
        Location dropLocation = findNearestSolidGround(player.getLocation());

        if (dropLocation != null) {
            // Drop the flag if the player holding it dies
            if (enemyFlag.getHolder() == player.getUniqueId()) {
                enemyFlag.dropFlag(dropLocation);
            }
        } else {
            // Return the flag to base if no solid ground is found
            enemyFlag.returnFlag();
        }
    }

    @GameEventHandler
    public void onFlagBreak(@NotNull BlockBreakEvent event) {
        // Cancels the event before the flag block is broken
        event.setCancelled(true);

        Player player = event.getPlayer();
        CTFTeam team = flagManager.getRound().getTeam(player.getUniqueId());

        if (team == null) {
            return;
        }

        TeamFlag flag = flagManager.getFlag(team);

        // Capture the flag if the player is on the opposite team
        if (flag.getTeam() != team) {
            flag.captureFlag(player.getUniqueId());
        }
    }

    @GameEventHandler
    public void onFlagHolderMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        CTFTeam team = flagManager.getRound().getTeam(player.getUniqueId());

        if (team == null) {
            return;
        }

        // Get the enemy team
        List<Team> allTeams = flagManager.getRound().getGame().getTeams();
        CTFTeam enemyTeam = (CTFTeam) allTeams.stream().filter(t -> t != team).findFirst().orElse(null);

        // Get the flag of the enemy team
        TeamFlag enemyFlag = flagManager.getFlag(enemyTeam);

        // Check if the player is holding the enemy team's flag
        if (enemyFlag.getHolder() == player.getUniqueId()) {
            // Set the flag's location to the player's location
            enemyFlag.setCurrentLocation(player.getLocation());

            // Check if the player is at the base
            if (player.getLocation().distance(enemyFlag.getReturnLocation()) <= 2) {
                // Return the flag
                enemyFlag.returnFlag();

                // Add a point to the player's team
                iteratePoints(team);
            }
        }
    }

    @GameEventHandler
    public void onFlagHolderQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CTFTeam team = flagManager.getRound().getTeam(player.getUniqueId());

        if (team == null) {
            return;
        }

        // Get the enemy team
        List<Team> allTeams = flagManager.getRound().getGame().getTeams();
        CTFTeam enemyTeam = (CTFTeam) allTeams.stream().filter(t -> t != team).findFirst().orElse(null);

        // Get the flag of the enemy team
        TeamFlag enemyFlag = flagManager.getFlag(enemyTeam);

        // Drop the flag if the player holding it disconnects
        if (enemyFlag.getHolder() == player.getUniqueId()) {
            Location dropLocation = findNearestSolidGround(player.getLocation());

            if (dropLocation != null) {
                enemyFlag.dropFlag(dropLocation);
            } else {
                enemyFlag.returnFlag();
            }
        }
    }

    /**
     * Iterates the points of the team.
     *
     * @param team The team to iterate the points for.
     */
    private void iteratePoints(@NotNull CTFTeam team) {
        int maxPoints = CTFRound.getMaxPoints();

        // If the team has reached the max points, end the round
        if (team.getPoints() == maxPoints) {
            flagManager.getRound().roundEnd(team);
            Bukkit.broadcastMessage("Team " + team.getTeamName() + " has won the game!");
        } else {
            team.setPoints(team.getPoints() + 1);
            Bukkit.broadcastMessage("Team " + team.getTeamName() + " has scored a point!");
        }
    }

    /**
     * Finds the nearest solid ground from the given location.
     * Excludes wool blocks. Goes down from the given location to Y=0.
     *
     * @param location The location to find the nearest solid ground from.
     * @return The nearest solid ground location. Null if not found.
     */
    private @Nullable Location findNearestSolidGround(@NotNull Location location) {
        int radius = 5;

        for (int y = location.getBlockY(); y >= 0; y--) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Location checkLocation = location.clone().add(x, y - location.getBlockY(), z);
                    Material blockType = checkLocation.getBlock().getType();

                    // Check if the block is solid and not wool
                    if (blockType.isSolid() && !blockType.name().contains("WOOL")) {
                        return checkLocation;
                    }
                }
            }
        }
        return null;
    }
}

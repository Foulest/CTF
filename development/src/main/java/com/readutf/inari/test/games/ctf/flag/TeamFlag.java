package com.readutf.inari.test.games.ctf.flag;

import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.test.InariDemo;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
class TeamFlag {

    private final Team team;
    private Location currentLocation;
    private Location returnLocation;

    private boolean atBase;
    private @Nullable UUID holder;

    TeamFlag(Team team, @NotNull Location currentLocation, @NotNull Location returnLocation) {
        this.team = team;
        this.currentLocation = currentLocation;
        this.returnLocation = returnLocation;

        // Create the flag block (banner) at the location
        createFlag(currentLocation);

        atBase = true;
        holder = null;
    }

    private void createFlag(@NotNull Location location) {
        Banner banner = (Banner) location.getBlock().getState();
        banner.setBaseColor(team.getColor().getDyeColor());
        banner.update();
    }

    void captureFlag(@NotNull UUID playerId) {
        // Remove the flag from the base location
        currentLocation.getBlock().setType(Material.AIR);

        holder = playerId;
        atBase = false;
    }

    void returnFlag() {
        // Move the flag back to the base location
        createFlag(currentLocation);

        holder = null;
        atBase = true;
        currentLocation = returnLocation;
    }

    void dropFlag(@NotNull Location dropLocation) {
        // Create the flag at the drop location
        createFlag(dropLocation);

        // Start a countdown before the flag is automatically returned
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!atBase && holder == null) {
                    returnFlag();
                    Bukkit.broadcastMessage("The flag has been automatically returned to the base!");
                }
            }
        }.runTaskLater(InariDemo.getInstance(), 20 * 30); // 30 seconds delay

        holder = null;
        atBase = false;
        currentLocation = dropLocation;
    }
}

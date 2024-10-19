package com.readutf.inari.core.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorldEditUtils {

    private static boolean isFawe;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        try {
            Class.forName("com.fastasyncworldedit.core.FaweAPI");
            isFawe = true;
        } catch (ClassNotFoundException ex) {
            isFawe = false;
        }
    }

    public static <T> CompletableFuture<T> runTask(JavaPlugin javaPlugin, Callable<T> runnable) {
        if (isFawe) {
            try {
                return CompletableFuture.completedFuture(runnable.call());
            } catch (Exception ex) {
                return CompletableFuture.failedFuture(ex);
            }
        } else {
            CompletableFuture<T> future = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(javaPlugin, () -> {
                try {
                    future.complete(runnable.call());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            });
            return future;
        }
    }

    public static @NotNull CuboidRegion toCuboidRegion(@NotNull WorldCuboid worldCuboid) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(worldCuboid.getWorld());
        BlockVector3 min = BlockVector3.at(worldCuboid.getMin().getX(), worldCuboid.getMin().getY(), worldCuboid.getMin().getZ());
        BlockVector3 max = BlockVector3.at(worldCuboid.getMax().getX(), worldCuboid.getMax().getY(), worldCuboid.getMax().getZ());
        return new CuboidRegion(world, min, max);
    }
}

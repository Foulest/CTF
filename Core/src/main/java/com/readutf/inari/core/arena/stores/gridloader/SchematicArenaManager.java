package com.readutf.inari.core.arena.stores.gridloader;

import com.google.gson.Gson;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.readutf.inari.core.arena.ActiveArena;
import com.readutf.inari.core.arena.stores.gridloader.grid.GridPositionManager;
import com.readutf.inari.core.arena.Arena;
import com.readutf.inari.core.arena.ArenaManager;
import com.readutf.inari.core.arena.exceptions.ArenaLoadException;
import com.readutf.inari.core.arena.exceptions.ArenaStoreException;
import com.readutf.inari.core.arena.marker.MarkerScanner;
import com.readutf.inari.core.arena.meta.ArenaMeta;
import com.readutf.inari.core.arena.stores.gridloader.loader.ArenaBuildLoader;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.logging.Logger;
import com.readutf.inari.core.logging.LoggerManager;
import com.readutf.inari.core.utils.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class SchematicArenaManager extends ArenaManager {

    private @Getter static final WorldCreator worldCreator;
    private @Getter static final World world;
    private static Logger logger = LoggerManager.getInstance().getLogger(SchematicArenaManager.class);

    static {

//        try {
////            FileUtils.deleteDirectory(new File(Bukkit.getServer().getWorldContainer().getParent(), "active_arenas"));
//            logger.fine("Deleted old active_arenas folder");
//        } catch (IOException e) {
//            logger.fine("Failed to delete old active_arenas folder");
//        }

        worldCreator = new WorldCreator("active_arenas");
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidChunkGenerator());
        world = worldCreator.createWorld();
        world.setAutoSave(false);
    }


    private final JavaPlugin javaPlugin;
    private final File arenasFolder;
    private final List<ArenaMeta> availableArenas;
    private final AtomicInteger currentOffset = new AtomicInteger(0);
    private final GridPositionManager gridPositionManager;
    private final ArenaBuildLoader buildLoader;

    public SchematicArenaManager(JavaPlugin javaPlugin, MarkerScanner markerScanner, ArenaBuildLoader buildLoader, File pluginFolder) {
        super(markerScanner);
        this.javaPlugin = javaPlugin;
        this.arenasFolder = setupArenasFolder(pluginFolder);
        this.availableArenas = loadAvailableArenas();
        this.buildLoader = buildLoader;
        this.gridPositionManager = new GridPositionManager(500);
    }

    @Override
    public void save(WorldCuboid worldCuboid, Arena arena) throws ArenaStoreException {

        File arenaFolder = new File(arenasFolder, arena.getName());
        if (arenaFolder.mkdirs()) logger.fine("Created arena folder");

        arena = arena.makeRelative();

        try {
            WorldEditUtils.runTask(javaPlugin, () -> {
                buildLoader.saveSchematic(worldCuboid, arenaFolder);
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new ArenaStoreException("Failed to save schematic.");
        }


        try {

            FileWriter writer = new FileWriter(new File(arenaFolder, "arena.json"));
            Game.getGson().toJson(arena, writer);
            writer.flush();
        } catch (IOException e) {
            throw new ArenaStoreException(e.getMessage());
        }

        availableArenas.add(arena.getArenaMeta());
    }

    @Override
    public ActiveArena load(ArenaMeta arenaMeta) throws ArenaLoadException {

        Arena arena;

        try {
            arena = loadArena(arenaMeta);
        } catch (IOException e) {
            throw new ArenaLoadException("Failed to parse json.", e);
        } catch (WorldEditException e) {
            throw new ArenaLoadException("Failed to load schematic.", e);
        }
        if (arena == null) throw new ArenaLoadException("Arena not found");


        return new ActiveArena(world, arena, this::unload);
    }

    @Override
    public void unload(Arena arena) {

        Cuboid bounds = arena.getBounds();
        Position min = bounds.getMin();


        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            BlockState blockState = BukkitAdapter.adapt(Material.AIR.createBlockData());
            Position max = bounds.getMax();
            editSession.setBlocks((Region) WorldEditUtils.toCuboidRegion(new WorldCuboid(world, min, max)), blockState);

            editSession.setFastMode(true);

            Operations.complete(editSession.commit());

            logger.debug("Filling " + min.getX() + ", " + min.getY() + ", " + min.getZ() + " - " + max.getX() + ", " + max.getY() + ", " + +max.getZ());
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }

        gridPositionManager.free(new GridPositionManager.GridSpace((int) min.getX(), (int) min.getZ()));

        logger.fine("Unloaded arena " + arena.getName());
    }

    public Arena loadArena(ArenaMeta arenaMeta) throws IOException, WorldEditException {
        File arenaFolder = new File(arenasFolder, arenaMeta.getName());
        if (!arenaFolder.exists()) return null;

        Arena arena = Game.getGson().fromJson(new FileReader(new File(arenaFolder, "arena.json")), Arena.class);

        GridPositionManager.GridSpace gridSpace = gridPositionManager.next();
        Position origin = new Position(gridSpace.getX(), 5, gridSpace.getZ());

        arena = arena.makeRelative(origin);

        buildLoader.pasteSchematic(world, arenaFolder, origin);


        return arena;
    }


    @Override
    public @Nullable List<ArenaMeta> findAvailableArenas(Predicate<ArenaMeta> predicate) {
        return availableArenas.stream().filter(predicate).toList();
    }

    public List<ArenaMeta> loadAvailableArenas() {
        List<ArenaMeta> available = new ArrayList<>();

        File[] files = arenasFolder.listFiles();
        if (files == null) return Collections.emptyList();
        for (File file : files) {
            if (file.isDirectory()) {
                File arenaFile = new File(file, "arena.json");
                if (arenaFile.exists()) {
                    try {
                        Arena arena = Game.getGson().fromJson(new FileReader(arenaFile), Arena.class);
                        available.add(arena.getArenaMeta());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return available;
    }

    @NotNull
    private File setupArenasFolder(File pluginFolder) {
        final File arenasFolder;
        arenasFolder = new File(pluginFolder, "arenas");
        if (arenasFolder.mkdirs()) {
            logger.fine("Created arenas folder");
        }
        return arenasFolder;
    }

    @Override
    public void shutdown() {
        Bukkit.unloadWorld(world, false);
    }

    public static class VoidChunkGenerator extends ChunkGenerator {

        @Override
        public List<BlockPopulator> getDefaultPopulators(World world) {return List.of();}

        @Override
        public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                  @NotNull ChunkData chunkData) {}

        @Override
        public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                    @NotNull ChunkData chunkData) {}

        @Override
        public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                    @NotNull ChunkData chunkData) {}

        @Override
        public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                  @NotNull ChunkData chunkData) {}

        @Override
        @Nullable
        public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {return new VoidBiomeProvider();}

        @Override
        public boolean canSpawn(World world, int x, int z) {return true;}

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {return new Location(world, 0, 100, 0);}
    }

    private static class VoidBiomeProvider extends BiomeProvider {

        @Override
        public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {return Biome.THE_VOID;}

        @Override
        public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {return List.of(Biome.THE_VOID);}

    }

}

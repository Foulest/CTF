package com.readutf.inari.core.arena.stores.schematic;

import com.readutf.inari.core.arena.ActiveArena;
import com.readutf.inari.core.arena.Arena;
import com.readutf.inari.core.arena.ArenaManager;
import com.readutf.inari.core.arena.GridPositionManager;
import com.readutf.inari.core.arena.exceptions.ArenaLoadException;
import com.readutf.inari.core.arena.exceptions.ArenaStoreException;
import com.readutf.inari.core.arena.marker.MarkerScanner;
import com.readutf.inari.core.arena.meta.ArenaMeta;
import com.readutf.inari.core.arena.stores.schematic.loader.ArenaBuildLoader;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.logging.Logger;
import com.readutf.inari.core.logging.LoggerFactory;
import com.readutf.inari.core.utils.Cuboid;
import com.readutf.inari.core.utils.Position;
import com.readutf.inari.core.utils.WorldCuboid;
import com.readutf.inari.core.utils.WorldEditUtils;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

@Getter
public class SchematicArenaManager extends ArenaManager {

    private static final int BUFFER_SIZE = 5;
    private static final Logger logger = LoggerFactory.getLogger(SchematicArenaManager.class);
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final WorldCreator worldCreator;
    private final World world;
    private final JavaPlugin javaPlugin;
    private final File arenasFolder;
    private final List<ArenaMeta> availableArenas;
    private final GridPositionManager gridPositionManager;
    private final ArenaBuildLoader buildLoader;
    private final Map<ArenaMeta, ArrayDeque<Arena>> cachedArenas = new HashMap<>();

    public SchematicArenaManager(JavaPlugin javaPlugin, MarkerScanner markerScanner, ArenaBuildLoader buildLoader, File pluginFolder) {
        super(markerScanner);
        this.javaPlugin = javaPlugin;
        arenasFolder = setupArenasFolder(pluginFolder);
        availableArenas = loadAvailableArenas();
        this.buildLoader = buildLoader;
        gridPositionManager = new GridPositionManager(500);

        worldCreator = new WorldCreator("active_arenas");
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidChunkGenerator());
        world = worldCreator.createWorld();
        world.setAutoSave(false);

        for (ArenaMeta availableArena : availableArenas) {
            ArrayDeque<Arena> arenas = new ArrayDeque<>();

            for (int i = 0; i < BUFFER_SIZE; i++) {
                Arena load;

                try {
                    load = loadArena(availableArena);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (load != null) {
                    arenas.add(load);
                }
            }

            cachedArenas.put(availableArena, arenas);
        }
    }

    @Override
    public void save(WorldCuboid worldCuboid, @NotNull Arena arena) throws ArenaStoreException {
        File arenaFolder = new File(arenasFolder, arena.getName());

        if (arenaFolder.mkdirs()) {
            logger.info("Created arena folder");
        }

        arena = arena.normalize();

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

            FileWriter writer = new FileWriter(new File(arenaFolder, "arena.json"), StandardCharsets.UTF_8);
            Game.getGson().toJson(arena, writer);
            writer.flush();
        } catch (IOException e) {
            throw new ArenaStoreException(e.getMessage());
        }

        availableArenas.add(arena.getArenaMeta());
    }

    @Override
    public CompletableFuture<ActiveArena> load(ArenaMeta arenaMeta) throws ArenaLoadException {
        ArrayDeque<Arena> arenas = cachedArenas.get(arenaMeta);

        if (!arenas.isEmpty()) {
            executorService.submit(() -> {
                Arena arena;

                try {
                    arena = loadArena(arenaMeta);
                } catch (IOException ex) {
                    logger.error("Failed to parse json", ex);
                    return;
                } catch (WorldEditException ex) {
                    logger.error("Failed to load schematic", ex);
                    return;
                }

                if (arena == null) {
                    logger.error("Arena not found");
                }
            });

            if (arenas.isEmpty()) {
                return CompletableFuture.failedFuture(new ArenaLoadException("Failed to load arena", null));
            }
            return CompletableFuture.completedFuture(new ActiveArena(world, arenas.poll(), this::unload));
        }

        Arena arena;

        try {
            arena = loadArena(arenaMeta);
        } catch (IOException ex) {
            throw new ArenaLoadException("Failed to parse json", ex);
        } catch (WorldEditException ex) {
            throw new ArenaLoadException("Failed to load schematic", ex);
        }

        if (arena == null) {
            throw new ArenaLoadException("Arena not found", null);
        }
        return CompletableFuture.completedFuture(new ActiveArena(world, arena, this::unload));
    }

    @Override
    public void unload(@NotNull Arena arena) {
        Cuboid bounds = arena.getBounds();
        Position min = bounds.getMin();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            BlockState blockState = BukkitAdapter.adapt(Material.AIR.createBlockData());
            Position max = bounds.getMax();

            editSession.setBlocks((Region) WorldEditUtils.toCuboidRegion(new WorldCuboid(world, min, max)), blockState);
            editSession.setFastMode(true);

            Operations.complete(editSession.commit());
            logger.debug("Filling " + min.getX() + ", " + min.getY() + ", " + min.getZ() + " - " + max.getX() + ", " + max.getY() + ", " + +max.getZ());
        } catch (MaxChangedBlocksException ex) {
            ex.printStackTrace();
        }

        gridPositionManager.free(new GridPositionManager.GridSpace((int) min.getX(), (int) min.getZ()));
        logger.info("Unloaded arena " + arena.getName());
    }

    /**
     * Loads an arena from the file system.
     *
     * @param arenaMeta The metadata of the arena to load
     * @throws IOException when an IO error occurs
     * @throws WorldEditException when a WorldEdit error occurs
     * @return The loaded arena
     */
    private @Nullable Arena loadArena(@NotNull ArenaMeta arenaMeta) throws IOException {
        File arenaFolder = new File(arenasFolder, arenaMeta.getName());

        if (!arenaFolder.exists()) {
            return null;
        }

        Arena arena = Game.getGson().fromJson(new FileReader(new File(arenaFolder, "arena.json"), StandardCharsets.UTF_8), Arena.class);
        GridPositionManager.GridSpace gridSpace = gridPositionManager.next();
        Position origin = new Position(gridSpace.getX(), 5, gridSpace.getZ());
        arena = arena.makeRelative(origin);

        buildLoader.pasteSchematic(world, arenaFolder, origin);
        return arena;
    }

    @Override
    public List<ArenaMeta> findAvailableArenas(Predicate<ArenaMeta> predicate) {
        List<ArenaMeta> list = new ArrayList<>();

        for (ArenaMeta arenaMeta : availableArenas) {
            if (predicate.test(arenaMeta)) {
                list.add(arenaMeta);
            }
        }
        return list;
    }

    private @NotNull List<ArenaMeta> loadAvailableArenas() {
        List<ArenaMeta> available = new ArrayList<>();
        File[] files = arenasFolder.listFiles();

        if (files == null) {
            return Collections.emptyList();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                File arenaFile = new File(file, "arena.json");

                if (arenaFile.exists()) {
                    try {
                        Arena arena = Game.getGson().fromJson(new FileReader(arenaFile, StandardCharsets.UTF_8), Arena.class);
                        available.add(arena.getArenaMeta());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return available;
    }

    @NotNull
    private static File setupArenasFolder(File pluginFolder) {
        File arenasFolder = new File(pluginFolder, "arenas");

        if (arenasFolder.mkdirs()) {
            logger.info("Created arenas folder");
        }
        return arenasFolder;
    }

    @Override
    public void shutdown() {
        Bukkit.unloadWorld(world, false);
    }

    public static class VoidChunkGenerator extends ChunkGenerator {

        @Override
        public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
            return List.of();
        }

        @Override
        public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                  @NotNull ChunkData chunkData) {
        }

        @Override
        public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                    @NotNull ChunkData chunkData) {
        }

        @Override
        public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                    @NotNull ChunkData chunkData) {
        }

        @Override
        public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
                                  @NotNull ChunkData chunkData) {
        }

        @Override
        @Nullable
        public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
            return new VoidBiomeProvider();
        }

        @Override
        public boolean canSpawn(@NotNull World world, int x, int z) {
            return true;
        }

        @Override
        public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
            return new Location(world, 0, 100, 0);
        }
    }

    private static class VoidBiomeProvider extends BiomeProvider {

        @Override
        public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
            return Biome.THE_VOID;
        }

        @Override
        @Contract(value = "_ -> new", pure = true)
        public @NotNull @Unmodifiable List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
            return List.of(Biome.THE_VOID);
        }
    }
}

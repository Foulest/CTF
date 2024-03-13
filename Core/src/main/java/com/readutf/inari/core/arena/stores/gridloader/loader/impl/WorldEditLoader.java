package com.readutf.inari.core.arena.stores.gridloader.loader.impl;

import com.readutf.inari.core.arena.exceptions.ArenaStoreException;
import com.readutf.inari.core.arena.stores.gridloader.loader.ArenaBuildLoader;
import com.readutf.inari.core.logging.GameLoggerFactory;
import com.readutf.inari.core.logging.Logger;
import com.readutf.inari.core.logging.LoggerFactory;
import com.readutf.inari.core.utils.Position;
import com.readutf.inari.core.utils.WorldCuboid;
import com.readutf.inari.core.utils.WorldEditUtils;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorldEditLoader implements ArenaBuildLoader {

    private static final Logger logger = LoggerFactory.getLogger(WorldEditLoader.class);

    private final Map<String, Clipboard> clipboards;

    public WorldEditLoader() {
        this.clipboards = new HashMap<>();
    }

    @Override
    public void pasteSchematic(World world, File arenaFolder, Position origin) throws IOException {


        long schematicStart = System.currentTimeMillis();
        Clipboard clipboard = clipboards.getOrDefault(arenaFolder.getName(), null);
        if(clipboard == null) {
            ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(new FileInputStream(new File(arenaFolder, "arena.schematic")));
            clipboard = reader.read();
            clipboards.put(arenaFolder.getName(), clipboard);
        }

        logger.info("Loaded schematic in " + (System.currentTimeMillis() - schematicStart) + "ms");
        long pasteStart = System.currentTimeMillis();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation pasteOperation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(origin.getX(), origin.getY(), origin.getZ()))
                    .ignoreAirBlocks(false)
                    .build();

            Operations.complete(pasteOperation);
        }

        logger.info("Pasted schematic at " + origin + " in " + (System.currentTimeMillis() - pasteStart) + "ms");
    }

    @Override
    public void saveSchematic(WorldCuboid worldCuboid, File arenaFolder) throws ArenaStoreException {

        long start = System.currentTimeMillis();

        CuboidRegion region = WorldEditUtils.toCuboidRegion(worldCuboid);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        forwardExtentCopy.setCopyingEntities(false);
        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            throw new ArenaStoreException(e.getMessage());
        }


        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(new File(arenaFolder, "arena.schematic")))) {
            writer.write(clipboard);
        } catch (IOException e) {
            throw new ArenaStoreException(e.getMessage());
        }

        logger.info("Saved schematic in " + (System.currentTimeMillis() - start) + "ms");
    }

}

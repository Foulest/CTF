package com.readutf.inari.core.arena;

import lombok.Getter;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public class ActiveArena extends Arena {

    private final World world;
    private final Consumer<Arena> freeArenaFunction;

    public ActiveArena(World world, @NotNull Arena arena, Consumer<Arena> freeArenaFunction) {
        super(arena.getName(), arena.getBounds(), arena.getArenaMeta(), arena.getMarkers());
        this.world = world;
        this.freeArenaFunction = freeArenaFunction;
    }

    public void free() {
        freeArenaFunction.accept(this);
    }
}

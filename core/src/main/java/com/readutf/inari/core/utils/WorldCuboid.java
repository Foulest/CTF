package com.readutf.inari.core.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Getter
public class WorldCuboid extends Cuboid {

    private final World world;

    public WorldCuboid(World world, Position min, Position max) {
        super(min, max);
        this.world = world;
    }

    public WorldCuboid(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = world;
    }

    public Cuboid toCuboid() {
        return new Cuboid(getMin(), getMax());
    }

    public boolean contains(@NotNull Location location) {
        return location.getWorld() == world && contains(new Position(location));
    }
}

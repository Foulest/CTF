package com.readutf.inari.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@AllArgsConstructor
public class Position {

    private final double x;
    private final double y;
    private final double z;

    /**
     * Converts a location to a position
     * @param location the location to convert
     */
    public Position(@NotNull Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
    }

    /**
     * Returns the current position with the X and Y centered
     * @return the new centered position
     */
    @JsonIgnore
    public Position center() {
        return new Position(getBlockX() + 0.5, getBlockY(), getBlockZ() + 0.5);
    }

    /**
     * Shortcut for {@link #Position(Location)}
     * @param location the location to convert
     * @return the new position
     */
    @Contract("_ -> new")
    public static @NotNull Position fromLocation(Location location) {
        return new Position(location);
    }

    /**
     * Converts the position to a location
     * @param world the world to convert to
     * @return the new location
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    /**
     * Adds the offset to the position
     * @param offsetX the x offset to add
     * @param offsetY the y offset to add
     * @param offsetZ the z offset to add
     * @return the new position
     */
    public Position add(double offsetX, double offsetY, double offsetZ) {
        return new Position(x + offsetX, y + offsetY, z + offsetZ);
    }

    /**
     * Subtracts the offset from the position
     * @param offsetX the x offset to subtract
     * @param offsetY the y offset to subtract
     * @param offsetZ the z offset to subtract
     * @return the new position
     */
    @Contract("_, _, _ -> new")
    private @NotNull Position subtract(double offsetX, double offsetY, double offsetZ) {
        return new Position(x - offsetX, y - offsetY, z - offsetZ);
    }

    /**
     * Adds the offset to the position
     * @param position the position to add
     * @return the new position
     */
    public Position add(@NotNull Position position) {
        return add(position.x, position.y, position.z);
    }

    /**
     * Subtracts the offset from the position
     * @param position the position to subtract
     * @return the new position
     */
    public Position subtract(@NotNull Position position) {
        return subtract(position.x, position.y, position.z);
    }

    /**
     * Returns the block X position
     * @return the block X position
     */
    @JsonIgnore
    public int getBlockX() {
        return (int) Math.floor(x);
    }

    /**
     * Returns the block Y position
     * @return the block Y position
     */
    @JsonIgnore
    public int getBlockY() {
        return (int) Math.floor(y);
    }

    /**
     * Returns the block Z position
     * @return the block Z position
     */
    @JsonIgnore
    public int getBlockZ() {
        return (int) Math.floor(z);
    }
}

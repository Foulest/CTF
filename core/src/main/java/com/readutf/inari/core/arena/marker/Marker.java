package com.readutf.inari.core.arena.marker;

import com.readutf.inari.core.utils.AngleUtils;
import com.readutf.inari.core.utils.JsonIgnore;
import com.readutf.inari.core.utils.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
@AllArgsConstructor
@SuppressWarnings("deprecation")
public class Marker {

    private final String name;
    private final Position position;
    private final Position offset;
    private final float yaw;

    @JsonIgnore
    public Location toLocation(World world) {
        Location location = position.add(offset).center().toLocation(world);
        location.setYaw(yaw);
        return location;
    }

    @JsonIgnore
    public Position getPositionWithOffset() {
        return position.add(offset);
    }

    public static @Nullable Marker parseFromSign(@NotNull Location location) {
        Block block = location.getBlock();

        if (!(block.getState() instanceof Sign sign)) {
            return null;
        }

        String[] lines = sign.getLines();

        if (!lines[0].equalsIgnoreCase("#marker")) {
            return null;
        }

        Position position = new Position(location);
        String nameLine = lines[1];
        float yaw = 0;

        if (sign.getBlockData() instanceof Rotatable rotatable) {
            yaw = AngleUtils.faceToYaw(rotatable.getRotation());
        }

        Position offset = new Position(0, 0, 0);

        String coordinateLine = lines[2];

        if (!coordinateLine.isBlank()) {
            String[] coordinateSplit = coordinateLine.split(",");

            if (coordinateSplit.length != 3) {
                return null;
            }

            double offsetX = Double.parseDouble(coordinateSplit[0]);
            double offsetY = Double.parseDouble(coordinateSplit[1]);
            double offsetZ = Double.parseDouble(coordinateSplit[2]);

            offset = new Position(offsetX, offsetY, offsetZ);
        }
        return new Marker(nameLine, position, offset, yaw);
    }
}

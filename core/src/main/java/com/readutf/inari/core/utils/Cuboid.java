package com.readutf.inari.core.utils;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Getter
@ToString
public class Cuboid implements Iterable<Position> {

    private final Position min;
    private final Position max;

    Cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        min = new Position(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        max = new Position(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public Cuboid(@NotNull Position min, @NotNull Position max) {
        this.min = new Position(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()));
        this.max = new Position(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()));
    }

    public boolean contains(@NotNull Position position) {
        return position.getX() >= min.getX()
                && position.getX() <= max.getX()
                && position.getY() >= min.getY()
                && position.getY() <= max.getY()
                && position.getZ() >= min.getZ()
                && position.getZ() <= max.getZ();
    }

    @NotNull
    @Override
    public Iterator<Position> iterator() {
        return new PositionIterator(min, max);
    }

    public static class PositionIterator implements Iterator<Position> {

        private final int xMin;
        private final int yMin;
        private final int xMax;
        private final int yMax;
        private final int zMax;
        private int x;
        private int y;
        private int z;

        PositionIterator(@NotNull Position min, @NotNull Position max) {
            xMin = min.getBlockX();
            yMin = min.getBlockY();
            xMax = max.getBlockX();
            yMax = max.getBlockY();
            zMax = max.getBlockZ();
            x = xMin;
            y = yMin;
            z = min.getBlockZ();
        }

        @Override
        public boolean hasNext() {
            return x <= xMax && y <= yMax && z <= zMax;
        }

        @Override
        public Position next() {
            Position position = new Position(x, y, z);

            if (x < xMax) {
                x++;
            } else if (y < yMax) {
                x = xMin;
                y++;
            } else if (z < zMax) {
                x = xMin;
                y = yMin;
                z++;
            } else {
                throw new NoSuchElementException("No more positions");
            }
            return position;
        }

        @Override
        public void forEachRemaining(Consumer<? super Position> action) {
            while (hasNext()) {
                action.accept(next());
            }
        }
    }
}

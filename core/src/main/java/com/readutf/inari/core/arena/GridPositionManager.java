package com.readutf.inari.core.arena;

import com.readutf.inari.core.logging.Logger;
import com.readutf.inari.core.logging.LoggerFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayDeque;

@ToString
public class GridPositionManager {

    private static final Logger logger = LoggerFactory.getLogger(GridPositionManager.class);

    private final ArrayDeque<GridSpace> recentlyFreed = new ArrayDeque<>();
    private final int spaceBetween;

    private int multiplier = 1;
    private int x;
    private int z;
    private int currentStep = 2;
    private int xStep = 1;
    private int zStep = 1;

    public GridPositionManager(int spaceBetween) {
        this.spaceBetween = spaceBetween;
    }

    public GridSpace next() {
        if (!recentlyFreed.isEmpty()) {
            return recentlyFreed.pollFirst();
        }

        if (xStep < currentStep) {
            x += (multiplier);
            xStep++;

            GridSpace gridSpace = new GridSpace(x, z).multiply(spaceBetween);
            logger.debug("Reserving arena grid space " + gridSpace);
            return gridSpace;
        }

        if (zStep < currentStep) {
            z += (multiplier);
            zStep++;

            GridSpace gridSpace = new GridSpace(x, z).multiply(spaceBetween);
            logger.debug("Reserving arena grid space " + gridSpace);
            return gridSpace;
        }

        multiplier = multiplier * -1;
        currentStep++;
        xStep = 1;
        zStep = 1;
        return next();
    }

    public void free(GridSpace space) {
        recentlyFreed.addLast(space);
        logger.debug("Freed arena grid space " + space);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    public static class GridSpace {

        private final int x;
        private final int z;

        GridSpace multiply(int multiplier) {
            return new GridSpace(x * multiplier, z * multiplier);
        }
    }
}

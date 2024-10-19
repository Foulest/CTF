package com.readutf.inari.core.arena.marker;

import com.readutf.inari.core.utils.WorldCuboid;

import java.util.List;

@FunctionalInterface
public interface MarkerScanner {

    List<Marker> scan(WorldCuboid cuboid);
}

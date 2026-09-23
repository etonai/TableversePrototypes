package com.tableverse.prototype01.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Reduces a grid-cell BFS path into a shorter list of straight-line world-space waypoints via
 * "string pulling": from the current anchor, greedily jump to the furthest cell still in a clear
 * line of sight, instead of visiting every intermediate cell. Lets characters cut corners and move
 * diagonally through open space while still routing around obstacles. No libGDX dependency.
 */
public final class PathSmoother {

    private PathSmoother() {
    }

    public static List<Waypoint> smooth(GameMap map, float startX, float startY, List<GridPoint> gridPath) {
        List<Waypoint> cellCenters = new ArrayList<>(gridPath.size());
        for (GridPoint gp : gridPath) {
            cellCenters.add(new Waypoint(map.gridCenterX(gp.x), map.gridCenterY(gp.y)));
        }

        List<Waypoint> result = new ArrayList<>();
        float anchorX = startX;
        float anchorY = startY;
        int index = 0;
        int size = cellCenters.size();

        while (index < size) {
            int furthest = index;
            for (int candidate = size - 1; candidate > index; candidate--) {
                Waypoint p = cellCenters.get(candidate);
                if (map.hasLineOfSight(anchorX, anchorY, p.x, p.y)) {
                    furthest = candidate;
                    break;
                }
            }
            Waypoint chosen = cellCenters.get(furthest);
            result.add(chosen);
            anchorX = chosen.x;
            anchorY = chosen.y;
            index = furthest + 1;
        }
        return result;
    }
}

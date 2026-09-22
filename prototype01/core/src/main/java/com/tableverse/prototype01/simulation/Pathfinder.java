package com.tableverse.prototype01.simulation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/** Breadth-first search over the map grid. No libGDX dependency. */
public final class Pathfinder {

    private static final int[][] NEIGHBOR_OFFSETS = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

    private Pathfinder() {
    }

    /** Returns an ordered list of grid cells from start to goal (inclusive), or null if unreachable. */
    public static List<GridPoint> findPath(GameMap map, GridPoint start, GridPoint goal) {
        if (!map.isWalkable(goal.x, goal.y)) {
            return null;
        }
        if (start.equals(goal)) {
            List<GridPoint> single = new ArrayList<>();
            single.add(start);
            return single;
        }

        Queue<GridPoint> frontier = new ArrayDeque<>();
        Map<GridPoint, GridPoint> cameFrom = new HashMap<>();
        frontier.add(start);
        cameFrom.put(start, start);

        while (!frontier.isEmpty()) {
            GridPoint current = frontier.poll();
            if (current.equals(goal)) {
                return reconstructPath(cameFrom, start, goal);
            }
            for (int[] offset : NEIGHBOR_OFFSETS) {
                GridPoint next = new GridPoint(current.x + offset[0], current.y + offset[1]);
                if (map.isWalkable(next.x, next.y) && !cameFrom.containsKey(next)) {
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
        }
        return null;
    }

    private static List<GridPoint> reconstructPath(Map<GridPoint, GridPoint> cameFrom, GridPoint start, GridPoint goal) {
        List<GridPoint> path = new ArrayList<>();
        GridPoint current = goal;
        while (!current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }
}

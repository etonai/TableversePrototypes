package com.tableverse.prototype01.simulation;

import java.util.List;

/**
 * A simulated character: position, movement state, and current destination. No libGDX dependency.
 *
 * Movement is reactive rather than precomputed: each tick, it heads straight for its destination if
 * that's currently a clear line of sight, and only consults the pathfinder (recalculating from its
 * current position) on ticks where a straight line is blocked. No path is stored between ticks.
 */
public class SimCharacter {
    public static final float SPEED = 120f; // world units per second

    /** How close (world units) to the destination counts as "arrived." */
    private static final float ARRIVAL_EPSILON = 0.5f;

    private final String id;
    private final String name;
    private float x;
    private float y;
    private CharacterState state = CharacterState.IDLE;
    private boolean hasDestination;
    private float destinationX;
    private float destinationY;

    public SimCharacter(String id, String name, float x, float y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public CharacterState getState() {
        return state;
    }

    public boolean hasDestination() {
        return hasDestination;
    }

    public float getDestinationX() {
        return destinationX;
    }

    public float getDestinationY() {
        return destinationY;
    }

    void startMoveOrder(float worldX, float worldY) {
        hasDestination = true;
        destinationX = worldX;
        destinationY = worldY;
        state = CharacterState.MOVING;
    }

    void update(GameMap map, float deltaSeconds) {
        if (state != CharacterState.MOVING) {
            return;
        }

        float targetX = destinationX;
        float targetY = destinationY;

        if (!map.hasLineOfSight(x, y, destinationX, destinationY)) {
            GridPoint startGrid = map.worldToGrid(x, y);
            GridPoint goalGrid = map.worldToGrid(destinationX, destinationY);
            List<GridPoint> gridPath = Pathfinder.findPath(map, startGrid, goalGrid);
            if (gridPath != null) {
                List<Waypoint> aimPoints = PathSmoother.smooth(map, x, y, gridPath);
                if (!aimPoints.isEmpty()) {
                    Waypoint aim = aimPoints.get(0);
                    targetX = aim.x;
                    targetY = aim.y;
                }
            }
            // If no path exists (shouldn't happen on this static map once an order is accepted),
            // fall back to heading at the destination directly rather than freezing in place.
        }

        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float step = SPEED * deltaSeconds;

        if (distance <= step) {
            x = targetX;
            y = targetY;
        } else {
            x += dx / distance * step;
            y += dy / distance * step;
        }

        float remainingX = destinationX - x;
        float remainingY = destinationY - y;
        if (remainingX * remainingX + remainingY * remainingY <= ARRIVAL_EPSILON * ARRIVAL_EPSILON) {
            x = destinationX;
            y = destinationY;
            state = CharacterState.IDLE;
            hasDestination = false;
        }
    }
}

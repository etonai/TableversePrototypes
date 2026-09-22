package com.tableverse.prototype01.simulation;

import java.util.ArrayList;
import java.util.List;

/** A simulated character: position, movement state, and pending path. No libGDX dependency. */
public class SimCharacter {
    public static final float SPEED = 120f; // world units per second

    private final String id;
    private final String name;
    private float x;
    private float y;
    private CharacterState state = CharacterState.IDLE;
    private final List<GridPoint> path = new ArrayList<>();
    private int nextWaypointIndex;
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

    void setDestination(float worldX, float worldY) {
        hasDestination = true;
        destinationX = worldX;
        destinationY = worldY;
    }

    void setPath(GameMap map, List<GridPoint> newPath) {
        path.clear();
        path.addAll(newPath);
        nextWaypointIndex = 0;
        // Skip the first waypoint if it is the cell the character is already standing in.
        if (!path.isEmpty()) {
            GridPoint first = path.get(0);
            if (Math.abs(map.gridCenterX(first.x) - x) < 1f && Math.abs(map.gridCenterY(first.y) - y) < 1f) {
                nextWaypointIndex = 1;
            }
        }
        state = nextWaypointIndex < path.size() ? CharacterState.MOVING : CharacterState.IDLE;
    }

    void update(GameMap map, float deltaSeconds) {
        if (state != CharacterState.MOVING) {
            return;
        }
        if (nextWaypointIndex >= path.size()) {
            state = CharacterState.IDLE;
            return;
        }
        GridPoint target = path.get(nextWaypointIndex);
        float targetX = map.gridCenterX(target.x);
        float targetY = map.gridCenterY(target.y);

        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float step = SPEED * deltaSeconds;

        if (distance <= step) {
            x = targetX;
            y = targetY;
            nextWaypointIndex++;
            if (nextWaypointIndex >= path.size()) {
                state = CharacterState.IDLE;
                path.clear();
                hasDestination = false;
            }
        } else {
            x += dx / distance * step;
            y += dy / distance * step;
        }
    }
}

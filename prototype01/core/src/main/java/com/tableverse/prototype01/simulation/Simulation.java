package com.tableverse.prototype01.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns map and character state, and advances it on a fixed step.
 * Plain Java, no libGDX dependency, so it can be instantiated and advanced without a window.
 */
public class Simulation {

    public static final float FIXED_STEP = 1f / 60f;

    private final GameMap map = new GameMap();
    private final List<SimCharacter> characters = new ArrayList<>();
    private String selectedCharacterId;
    private boolean paused;
    private float accumulator;

    public Simulation() {
        characters.add(new SimCharacter("alpha", "Alpha", map.gridCenterX(1), map.gridCenterY(1)));
        characters.add(new SimCharacter("beta", "Beta", map.gridCenterX(10), map.gridCenterY(7)));
    }

    public GameMap getMap() {
        return map;
    }

    public List<SimCharacter> getCharacters() {
        return characters;
    }

    public SimCharacter getCharacter(String id) {
        for (SimCharacter c : characters) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public String getSelectedCharacterId() {
        return selectedCharacterId;
    }

    public SimCharacter getSelectedCharacter() {
        return getCharacter(selectedCharacterId);
    }

    public boolean isPaused() {
        return paused;
    }

    /** Selects the character at the given world point, or clears selection if none is there. Returns true if a character was selected. */
    public boolean selectAt(float worldX, float worldY, float pickRadius) {
        for (SimCharacter c : characters) {
            float dx = c.getX() - worldX;
            float dy = c.getY() - worldY;
            if (dx * dx + dy * dy <= pickRadius * pickRadius) {
                selectedCharacterId = c.getId();
                return true;
            }
        }
        selectedCharacterId = null;
        return false;
    }

    public void clearSelection() {
        selectedCharacterId = null;
    }

    /**
     * Orders the currently selected character to move to the given world point.
     * Returns true if a path was found and the order was accepted; false if no character
     * is selected or the destination is unreachable (in which case the current order is unchanged).
     */
    public boolean issueMoveOrder(float worldX, float worldY) {
        SimCharacter selected = getSelectedCharacter();
        if (selected == null) {
            return false;
        }
        GridPoint start = map.worldToGrid(selected.getX(), selected.getY());
        GridPoint goal = map.worldToGrid(worldX, worldY);
        List<GridPoint> path = Pathfinder.findPath(map, start, goal);
        if (path == null) {
            return false;
        }
        selected.setPath(map, path);
        selected.setDestination(worldX, worldY);
        return true;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void togglePaused() {
        this.paused = !this.paused;
    }

    /** Advances the simulation by the given amount of real time, using a fixed step internally. */
    public void update(float deltaSeconds) {
        if (paused) {
            return;
        }
        accumulator += deltaSeconds;
        while (accumulator >= FIXED_STEP) {
            for (SimCharacter c : characters) {
                c.update(map, FIXED_STEP);
            }
            accumulator -= FIXED_STEP;
        }
    }
}

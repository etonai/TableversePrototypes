package com.tableverse.prototype01.simulation;

/** An immutable world-space point used as a movement waypoint. No libGDX dependency. */
public final class Waypoint {
    public final float x;
    public final float y;

    public Waypoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

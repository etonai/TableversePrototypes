package com.tableverse.prototype01.simulation;

/** An immutable integer grid cell coordinate. No libGDX dependency. */
public final class GridPoint {
    public final int x;
    public final int y;

    public GridPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridPoint)) return false;
        GridPoint other = (GridPoint) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

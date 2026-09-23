package com.tableverse.prototype01.simulation;

/** Fixed single-screen room: a walkable grid with a few blocking obstacle cells. No libGDX dependency. */
public class GameMap {
    public static final int WIDTH = 12;
    public static final int HEIGHT = 9;
    public static final float CELL_SIZE = 64f;

    private final boolean[][] blocked = new boolean[WIDTH][HEIGHT];

    public GameMap() {
        // A simple obstacle wall with a gap, plus a couple of standalone blocks.
        for (int y = 2; y <= 5; y++) {
            if (y == 4) continue; // gap in the wall
            blocked[5][y] = true;
        }
        blocked[8][6] = true;
        blocked[8][7] = true;
        blocked[2][1] = true;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    public boolean isBlocked(int x, int y) {
        if (!isInBounds(x, y)) return true;
        return blocked[x][y];
    }

    public boolean isWalkable(int x, int y) {
        return !isBlocked(x, y);
    }

    public GridPoint worldToGrid(float worldX, float worldY) {
        return new GridPoint((int) Math.floor(worldX / CELL_SIZE), (int) Math.floor(worldY / CELL_SIZE));
    }

    public float gridCenterX(int gx) {
        return gx * CELL_SIZE + CELL_SIZE / 2f;
    }

    public float gridCenterY(int gy) {
        return gy * CELL_SIZE + CELL_SIZE / 2f;
    }

    /**
     * Returns true if a straight line between the two world points passes only through walkable cells.
     *
     * Walks every grid cell the segment actually passes through (a grid/voxel traversal, not point
     * sampling), so it can't miss a thin corner-clip through a blocked cell the way fixed-interval
     * sampling can when the segment only grazes a small sliver of that cell.
     */
    public boolean hasLineOfSight(float x0, float y0, float x1, float y1) {
        int gx = (int) Math.floor(x0 / CELL_SIZE);
        int gy = (int) Math.floor(y0 / CELL_SIZE);
        int gx1 = (int) Math.floor(x1 / CELL_SIZE);
        int gy1 = (int) Math.floor(y1 / CELL_SIZE);

        if (!isWalkable(gx, gy)) {
            return false;
        }

        float dirX = x1 - x0;
        float dirY = y1 - y0;
        if (Math.abs(dirX) < 1e-6f && Math.abs(dirY) < 1e-6f) {
            return true;
        }

        int stepX = dirX > 0 ? 1 : (dirX < 0 ? -1 : 0);
        int stepY = dirY > 0 ? 1 : (dirY < 0 ? -1 : 0);

        float tMaxX;
        float tDeltaX;
        if (stepX != 0) {
            float nextBoundaryX = (stepX > 0) ? (gx + 1) * CELL_SIZE : gx * CELL_SIZE;
            tMaxX = (nextBoundaryX - x0) / dirX;
            tDeltaX = CELL_SIZE / Math.abs(dirX);
        } else {
            tMaxX = Float.POSITIVE_INFINITY;
            tDeltaX = Float.POSITIVE_INFINITY;
        }

        float tMaxY;
        float tDeltaY;
        if (stepY != 0) {
            float nextBoundaryY = (stepY > 0) ? (gy + 1) * CELL_SIZE : gy * CELL_SIZE;
            tMaxY = (nextBoundaryY - y0) / dirY;
            tDeltaY = CELL_SIZE / Math.abs(dirY);
        } else {
            tMaxY = Float.POSITIVE_INFINITY;
            tDeltaY = Float.POSITIVE_INFINITY;
        }

        while (gx != gx1 || gy != gy1) {
            if (tMaxX < tMaxY) {
                tMaxX += tDeltaX;
                gx += stepX;
            } else if (tMaxY < tMaxX) {
                tMaxY += tDeltaY;
                gy += stepY;
            } else {
                // Passing exactly through a corner: treat it as blocked if either flanking cell is,
                // matching the conservative choice a non-zero-radius character would need anyway.
                if (!isWalkable(gx + stepX, gy) || !isWalkable(gx, gy + stepY)) {
                    return false;
                }
                tMaxX += tDeltaX;
                tMaxY += tDeltaY;
                gx += stepX;
                gy += stepY;
            }
            if (!isWalkable(gx, gy)) {
                return false;
            }
        }
        return true;
    }
}

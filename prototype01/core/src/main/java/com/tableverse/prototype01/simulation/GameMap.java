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
}

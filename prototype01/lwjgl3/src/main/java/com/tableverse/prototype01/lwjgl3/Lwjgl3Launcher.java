package com.tableverse.prototype01.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.tableverse.prototype01.Prototype01Game;
import com.tableverse.prototype01.simulation.GameMap;

/** Launches the desktop (LWJGL3) build of Prototype 01. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TableverseRPG - Prototype 01");
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setResizable(true);
        int width = (int) (GameMap.WIDTH * GameMap.CELL_SIZE);
        int height = (int) (GameMap.HEIGHT * GameMap.CELL_SIZE);
        config.setWindowedMode(width, height);
        new Lwjgl3Application(new Prototype01Game(), config);
    }
}

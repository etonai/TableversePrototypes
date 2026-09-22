package com.tableverse.prototype01.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.tableverse.prototype01.simulation.CharacterState;
import com.tableverse.prototype01.simulation.GameMap;
import com.tableverse.prototype01.simulation.SimCharacter;
import com.tableverse.prototype01.simulation.Simulation;

/** Draws the room, obstacles, characters, selection feedback, and destination marker from a read-only view of the simulation. */
public class WorldRenderer {

    private static final float CHARACTER_RADIUS = 18f;
    public static final float PICK_RADIUS = CHARACTER_RADIUS + 4f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();

    public void render(Simulation simulation, OrthographicCamera camera, float unreachableMarkerX, float unreachableMarkerY, boolean showUnreachableMarker) {
        shapes.setProjectionMatrix(camera.combined);

        GameMap map = simulation.getMap();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.16f, 0.18f, 0.22f, 1f);
        shapes.rect(0, 0, GameMap.WIDTH * GameMap.CELL_SIZE, GameMap.HEIGHT * GameMap.CELL_SIZE);

        shapes.setColor(0.35f, 0.12f, 0.12f, 1f);
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                if (map.isBlocked(x, y)) {
                    shapes.rect(x * GameMap.CELL_SIZE, y * GameMap.CELL_SIZE, GameMap.CELL_SIZE, GameMap.CELL_SIZE);
                }
            }
        }

        for (SimCharacter c : simulation.getCharacters()) {
            if (c.hasDestination()) {
                shapes.setColor(Color.GOLD);
                shapes.circle(c.getDestinationX(), c.getDestinationY(), 6f);
            }
        }

        if (showUnreachableMarker) {
            shapes.setColor(Color.FIREBRICK);
            shapes.rectLine(unreachableMarkerX - 8, unreachableMarkerY - 8, unreachableMarkerX + 8, unreachableMarkerY + 8, 3f);
            shapes.rectLine(unreachableMarkerX - 8, unreachableMarkerY + 8, unreachableMarkerX + 8, unreachableMarkerY - 8, 3f);
        }

        for (SimCharacter c : simulation.getCharacters()) {
            boolean selected = c.getId().equals(simulation.getSelectedCharacterId());
            if (selected) {
                shapes.setColor(Color.WHITE);
                shapes.circle(c.getX(), c.getY(), CHARACTER_RADIUS + 5f);
            }
            shapes.setColor(c.getState() == CharacterState.MOVING ? Color.SKY : Color.FOREST);
            shapes.circle(c.getX(), c.getY(), CHARACTER_RADIUS);
        }
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (SimCharacter c : simulation.getCharacters()) {
            font.setColor(Color.WHITE);
            font.draw(batch, c.getName(), c.getX() - CHARACTER_RADIUS, c.getY() + CHARACTER_RADIUS + 16f);
        }
        if (simulation.isPaused()) {
            font.setColor(Color.GOLD);
            font.draw(batch, "PAUSED", GameMap.WIDTH * GameMap.CELL_SIZE / 2f - 30f, GameMap.HEIGHT * GameMap.CELL_SIZE - 12f);
        }
        batch.end();
    }

    public void resize(OrthographicCamera camera) {
        // Camera/viewport recalculation is handled by the owning viewport; nothing cached here.
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}

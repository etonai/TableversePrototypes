package com.tableverse.prototype01;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tableverse.prototype01.hud.DefaultSkinFactory;
import com.tableverse.prototype01.hud.GameHud;
import com.tableverse.prototype01.simulation.GameMap;
import com.tableverse.prototype01.simulation.Simulation;
import com.tableverse.prototype01.view.WorldInputProcessor;
import com.tableverse.prototype01.view.WorldRenderer;

/** Wires the simulation, world view/controls, and HUD together; owns the fixed-step update loop. */
public class Prototype01Game extends ApplicationAdapter {

    private static final float UNREACHABLE_MARKER_SECONDS = 0.6f;

    private Simulation simulation;
    private OrthographicCamera worldCamera;
    private Viewport worldViewport;
    private WorldRenderer worldRenderer;

    private Skin skin;
    private GameHud hud;

    private float unreachableMarkerX;
    private float unreachableMarkerY;
    private float unreachableMarkerTimer;

    @Override
    public void create() {
        simulation = new Simulation();

        float worldWidth = GameMap.WIDTH * GameMap.CELL_SIZE;
        float worldHeight = GameMap.HEIGHT * GameMap.CELL_SIZE;
        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(worldWidth, worldHeight, worldCamera);
        worldRenderer = new WorldRenderer();

        skin = DefaultSkinFactory.create();
        hud = new GameHud(skin);
        hud.setPauseClickedListener(() -> simulation.togglePaused());

        WorldInputProcessor worldInput = new WorldInputProcessor(simulation, worldViewport, this::showUnreachableMarker);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(worldInput);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void showUnreachableMarker(float worldX, float worldY) {
        unreachableMarkerX = worldX;
        unreachableMarkerY = worldY;
        unreachableMarkerTimer = UNREACHABLE_MARKER_SECONDS;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        simulation.update(delta);
        if (unreachableMarkerTimer > 0f) {
            unreachableMarkerTimer -= delta;
        }
        hud.update(simulation);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        worldViewport.apply();
        worldRenderer.render(simulation, worldCamera, unreachableMarkerX, unreachableMarkerY, unreachableMarkerTimer > 0f);

        hud.getStage().act(delta);
        hud.getStage().draw();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        hud.dispose();
        skin.dispose();
    }
}

package com.tableverse.prototype01.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tableverse.prototype01.simulation.Simulation;

/** Converts screen clicks to world coordinates and translates them into simulation select/order actions. */
public class WorldInputProcessor extends InputAdapter {

    /** Notified when a right-click order is rejected because the destination is unreachable. */
    public interface UnreachableListener {
        void onUnreachable(float worldX, float worldY);
    }

    private final Simulation simulation;
    private final Viewport worldViewport;
    private final UnreachableListener unreachableListener;
    private final Vector2 tmp = new Vector2();

    public WorldInputProcessor(Simulation simulation, Viewport worldViewport, UnreachableListener unreachableListener) {
        this.simulation = simulation;
        this.worldViewport = worldViewport;
        this.unreachableListener = unreachableListener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        tmp.set(screenX, screenY);
        worldViewport.unproject(tmp);

        if (button == Input.Buttons.LEFT) {
            simulation.selectAt(tmp.x, tmp.y, WorldRenderer.PICK_RADIUS);
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            if (simulation.getSelectedCharacter() == null) {
                return true;
            }
            boolean accepted = simulation.issueMoveOrder(tmp.x, tmp.y);
            if (!accepted && unreachableListener != null) {
                unreachableListener.onUnreachable(tmp.x, tmp.y);
            }
            return true;
        }
        return false;
    }
}

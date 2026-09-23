package com.tableverse.prototype01.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tableverse.prototype01.simulation.Simulation;

/**
 * Converts screen clicks to world coordinates and translates them into simulation select/order actions.
 * Also handles mouse-wheel zoom and right-click-drag panning of the world camera; the HUD uses its own
 * separate camera/viewport, so neither zoom nor panning here ever affects the status panel's on-screen size.
 */
public class WorldInputProcessor extends InputAdapter {

    public static final float MIN_ZOOM = 0.2f;
    public static final float MAX_ZOOM = 4f;
    private static final float ZOOM_STEP = 0.1f;

    /** Screen-pixel distance a right-click must travel before it's treated as a pan instead of a move order. */
    private static final float DRAG_THRESHOLD_PX = 8f;

    /** Notified when a right-click order is rejected because the destination is unreachable. */
    public interface UnreachableListener {
        void onUnreachable(float worldX, float worldY);
    }

    private final Simulation simulation;
    private final Viewport worldViewport;
    private final OrthographicCamera worldCamera;
    private final UnreachableListener unreachableListener;
    private final Vector2 tmp = new Vector2();
    private final Vector2 dragFrom = new Vector2();
    private final Vector2 dragTo = new Vector2();

    private boolean rightButtonDown;
    private boolean rightDragging;
    private float rightDownScreenX;
    private float rightDownScreenY;
    private float lastDragScreenX;
    private float lastDragScreenY;

    public WorldInputProcessor(Simulation simulation, Viewport worldViewport, OrthographicCamera worldCamera, UnreachableListener unreachableListener) {
        this.simulation = simulation;
        this.worldViewport = worldViewport;
        this.worldCamera = worldCamera;
        this.unreachableListener = unreachableListener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            tmp.set(screenX, screenY);
            worldViewport.unproject(tmp);
            simulation.selectAt(tmp.x, tmp.y, WorldRenderer.PICK_RADIUS);
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            rightButtonDown = true;
            rightDragging = false;
            rightDownScreenX = screenX;
            rightDownScreenY = screenY;
            lastDragScreenX = screenX;
            lastDragScreenY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!rightButtonDown) {
            return false;
        }

        float dx = screenX - rightDownScreenX;
        float dy = screenY - rightDownScreenY;
        if (!rightDragging && dx * dx + dy * dy >= DRAG_THRESHOLD_PX * DRAG_THRESHOLD_PX) {
            rightDragging = true;
        }

        if (rightDragging) {
            dragFrom.set(lastDragScreenX, lastDragScreenY);
            worldViewport.unproject(dragFrom);
            dragTo.set(screenX, screenY);
            worldViewport.unproject(dragTo);

            worldCamera.position.x -= dragTo.x - dragFrom.x;
            worldCamera.position.y -= dragTo.y - dragFrom.y;
            worldCamera.update();
        }

        lastDragScreenX = screenX;
        lastDragScreenY = screenY;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.RIGHT) {
            return false;
        }

        if (!rightDragging && simulation.getSelectedCharacter() != null) {
            tmp.set(screenX, screenY);
            worldViewport.unproject(tmp);
            boolean accepted = simulation.issueMoveOrder(tmp.x, tmp.y);
            if (!accepted && unreachableListener != null) {
                unreachableListener.onUnreachable(tmp.x, tmp.y);
            }
        }

        rightButtonDown = false;
        rightDragging = false;
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float zoom = worldCamera.zoom + amountY * ZOOM_STEP;
        worldCamera.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        worldCamera.update();
        return true;
    }
}
